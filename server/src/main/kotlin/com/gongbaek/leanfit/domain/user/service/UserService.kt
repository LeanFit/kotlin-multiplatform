package com.gongbaek.leanfit.domain.user.service

import com.gongbaek.leanfit.domain.subscription.repository.SubscriptionRepository
import com.gongbaek.leanfit.domain.user.dto.request.GuestAuthRequest
import com.gongbaek.leanfit.domain.user.dto.request.RefreshTokenRequest
import com.gongbaek.leanfit.domain.user.dto.request.SocialAuthRequest
import com.gongbaek.leanfit.domain.user.dto.request.UpdateUserRequest
import com.gongbaek.leanfit.domain.user.dto.request.UpdateUserSettingsRequest
import com.gongbaek.leanfit.domain.user.dto.response.AuthResponse
import com.gongbaek.leanfit.domain.user.dto.response.TokenResponse
import com.gongbaek.leanfit.domain.user.dto.response.UserResponse
import com.gongbaek.leanfit.domain.user.dto.response.UserSettingsResponse
import com.gongbaek.leanfit.domain.user.dto.response.toResponse
import com.gongbaek.leanfit.domain.user.entity.AuthProvider
import com.gongbaek.leanfit.domain.user.entity.User
import com.gongbaek.leanfit.domain.user.repository.UserRepository
import com.gongbaek.leanfit.domain.user.repository.UserSettingsRepository
import com.gongbaek.leanfit.global.exception.BusinessException
import com.gongbaek.leanfit.global.exception.ErrorCode
import com.gongbaek.leanfit.global.security.JwtProvider
import com.gongbaek.leanfit.global.security.RefreshTokenRepository
import java.util.UUID

class UserService(
    private val userRepository: UserRepository = UserRepository(),
    private val userSettingsRepository: UserSettingsRepository = UserSettingsRepository(),
    private val refreshTokenRepository: RefreshTokenRepository = RefreshTokenRepository(),
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepository(),
) {
    /**
     * 게스트 인증
     * POST /auth/guest
     */
    fun authenticateGuest(request: GuestAuthRequest): AuthResponse {
        val existingUser = userRepository.findByDeviceId(request.deviceId)

        val user =
            existingUser ?: run {
                val newUser = userRepository.createGuestUser(request.deviceId)
                userSettingsRepository.createDefault(newUser.id)
                newUser
            }

        return createAuthResponse(user, isNewUser = existingUser == null)
    }

    /**
     * 소셜 로그인
     * POST /auth/social
     */
    fun authenticateSocial(request: SocialAuthRequest): AuthResponse {
        val provider = AuthProvider.valueOf(request.provider)

        // TODO: Firebase Admin SDK로 idToken 검증 후 providerId, email 추출
        // 현재는 임시로 idToken을 providerId로 사용
        val providerId = request.idToken
        val email: String? = null
        val nickname: String? = null

        val existingUser = userRepository.findByProviderAndProviderId(provider, providerId)

        if (existingUser != null) {
            return createAuthResponse(existingUser, isNewUser = false)
        }

        // 이메일로 기존 다른 소셜 계정 확인
        email?.let {
            val userWithEmail = userRepository.findByEmail(it)
            if (userWithEmail != null && userWithEmail.provider != provider) {
                throw BusinessException(ErrorCode.USER_ALREADY_EXISTS)
            }
        }

        // 신규 유저 생성
        val newUser = userRepository.createSocialUser(provider, providerId, email, nickname)
        userSettingsRepository.createDefault(newUser.id)

        // 게스트 데이터 병합
        var mergedFromGuest = false
        request.deviceId?.let { deviceId ->
            val guestUser = userRepository.findByDeviceId(deviceId)
            if (guestUser != null) {
                // 게스트 유저의 구독 데이터를 새 유저로 이전
                subscriptionRepository.transferSubscriptions(guestUser.id, newUser.id)
                // 게스트 유저 삭제
                userSettingsRepository.deleteByUserId(guestUser.id)
                userRepository.deleteGuestUser(deviceId)
                mergedFromGuest = true
            }
        }

        return createAuthResponse(newUser, isNewUser = true, mergedFromGuest = mergedFromGuest)
    }

    /**
     * 토큰 갱신
     * POST /auth/refresh
     */
    fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        val userId =
            JwtProvider.getUserIdFromRefreshToken(request.refreshToken)
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

        val userIdUUID = UUID.fromString(userId)
        val tokenHash = JwtProvider.hashToken(request.refreshToken)

        // 저장된 토큰 검증
        refreshTokenRepository.findValidToken(userIdUUID, tokenHash)
            ?: throw BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN)

        val user =
            userRepository.findById(userIdUUID)
                ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        // 기존 토큰 무효화
        refreshTokenRepository.invalidateToken(userIdUUID, tokenHash)

        // 새 토큰 발급
        val newAccessToken = JwtProvider.generateAccessToken(user.id.toString())
        val newRefreshToken = JwtProvider.generateRefreshToken(user.id.toString())
        val newTokenHash = JwtProvider.hashToken(newRefreshToken)

        // 새 리프레시 토큰 저장
        refreshTokenRepository.save(user.id, newTokenHash)

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = JwtProvider.getAccessTokenExpiresIn(),
        )
    }

    /**
     * 로그아웃
     * POST /auth/logout
     */
    fun logout(
        userId: UUID,
        refreshToken: String,
    ) {
        val tokenHash = JwtProvider.hashToken(refreshToken)
        refreshTokenRepository.invalidateToken(userId, tokenHash)
    }

    /**
     * 내 정보 조회
     * GET /users/me
     */
    fun getMyInfo(userId: UUID): UserResponse {
        val user =
            userRepository.findById(userId)
                ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        val settings = userSettingsRepository.findByUserId(userId)

        return user.toResponse(settings)
    }

    /**
     * 닉네임 수정 (낙관적 잠금 적용)
     * PATCH /users/me
     */
    fun updateNickname(
        userId: UUID,
        request: UpdateUserRequest,
    ): UserResponse {
        validateNickname(request.nickname)

        val currentUser =
            userRepository.findById(userId)
                ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        val updatedUser = userRepository.updateNickname(userId, request.nickname, currentUser.version)

        return updatedUser.toResponse()
    }

    private fun validateNickname(nickname: String) {
        // 닉네임 길이 검증 (1~10자)
        if (nickname.length !in 1..10) {
            throw BusinessException(ErrorCode.INVALID_NICKNAME)
        }

        // 금칙어 필터링
        val forbiddenWords = listOf("admin", "관리자", "운영자", "시스템")
        if (forbiddenWords.any { nickname.contains(it, ignoreCase = true) }) {
            throw BusinessException(ErrorCode.FORBIDDEN_NICKNAME)
        }
    }

    /**
     * 설정 변경
     * PATCH /users/me/settings
     */
    fun updateSettings(
        userId: UUID,
        request: UpdateUserSettingsRequest,
    ): UserSettingsResponse {
        val settings =
            userSettingsRepository.update(
                userId = userId,
                notificationEnabled = request.notificationEnabled,
                notificationTime = request.notificationTime,
                dDayAlerts = request.dDayAlerts,
            ) ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        return settings.toResponse()
    }

    /**
     * 탈퇴
     * DELETE /users/me
     */
    fun deleteUser(userId: UUID) {
        // 구독 데이터 삭제
        subscriptionRepository.deleteAllByUserId(userId)
        // 설정 삭제
        userSettingsRepository.deleteByUserId(userId)
        // 리프레시 토큰 무효화
        refreshTokenRepository.invalidateAllTokens(userId)
        // 사용자 소프트 삭제
        userRepository.softDelete(userId)
    }

    private fun createAuthResponse(
        user: User,
        isNewUser: Boolean,
        mergedFromGuest: Boolean = false,
    ): AuthResponse {
        val accessToken = JwtProvider.generateAccessToken(user.id.toString())
        val refreshToken = JwtProvider.generateRefreshToken(user.id.toString())

        // 리프레시 토큰 저장
        val tokenHash = JwtProvider.hashToken(refreshToken)
        refreshTokenRepository.save(user.id, tokenHash)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = JwtProvider.getAccessTokenExpiresIn(),
            isNewUser = isNewUser,
            mergedFromGuest = mergedFromGuest,
            user = user.toResponse(),
        )
    }
}
