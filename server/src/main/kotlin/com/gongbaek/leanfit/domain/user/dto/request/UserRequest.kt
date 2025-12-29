package com.gongbaek.leanfit.domain.user.dto.request

import kotlinx.serialization.Serializable

/**
 * 게스트 인증 요청
 * POST /auth/guest
 */
@Serializable
data class GuestAuthRequest(
    val deviceId: String,
) {
    init {
        require(deviceId.isNotBlank()) { "deviceId must not be blank" }
    }
}

/**
 * 소셜 로그인 요청
 * POST /auth/social
 */
@Serializable
data class SocialAuthRequest(
    val provider: String,
    val idToken: String,
    val deviceId: String? = null,
) {
    init {
        require(provider.isNotBlank()) { "provider must not be blank" }
        require(provider in listOf("GOOGLE", "KAKAO", "APPLE")) { "provider must be GOOGLE, KAKAO, or APPLE" }
        require(idToken.isNotBlank()) { "idToken must not be blank" }
    }
}

/**
 * 토큰 갱신 요청
 * POST /auth/refresh
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
) {
    init {
        require(refreshToken.isNotBlank()) { "refreshToken must not be blank" }
    }
}

/**
 * 로그아웃 요청
 * POST /auth/logout
 */
@Serializable
data class LogoutRequest(
    val refreshToken: String,
) {
    init {
        require(refreshToken.isNotBlank()) { "refreshToken must not be blank" }
    }
}

/**
 * 사용자 정보 수정 요청
 * PATCH /users/me
 */
@Serializable
data class UpdateUserRequest(
    val nickname: String,
) {
    init {
        require(nickname.isNotBlank()) { "nickname must not be blank" }
        require(nickname.length in 1..10) { "nickname must be 1~10 characters" }
    }
}

/**
 * 사용자 설정 수정 요청
 * PATCH /users/me/settings
 */
@Serializable
data class UpdateUserSettingsRequest(
    val notificationEnabled: Boolean? = null,
    val notificationTime: String? = null,
    val dDayAlerts: List<Int>? = null,
)

/**
 * 탈퇴 요청
 * DELETE /users/me
 */
@Serializable
data class DeleteUserRequest(
    val confirmText: String,
) {
    init {
        require(confirmText == "탈퇴") { "confirmText must be '탈퇴'" }
    }
}
