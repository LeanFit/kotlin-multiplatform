package com.gongbaek.leanfit.domain.user.dto.response

import com.gongbaek.leanfit.domain.user.entity.User
import com.gongbaek.leanfit.domain.user.entity.UserSettings
import kotlinx.serialization.Serializable

/**
 * 인증 응답
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val isNewUser: Boolean = false,
    val mergedFromGuest: Boolean = false,
    val user: UserResponse,
)

/**
 * 토큰 갱신 응답
 */
@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)

/**
 * 사용자 정보 응답
 */
@Serializable
data class UserResponse(
    val id: String,
    val nickname: String?,
    val isGuest: Boolean,
    val provider: String?,
    val email: String?,
    val createdAt: String,
    val settings: UserSettingsResponse? = null,
) {
    companion object {
        fun from(
            user: User,
            settings: UserSettings? = null,
        ): UserResponse =
            UserResponse(
                id = user.id.toString(),
                nickname = user.nickname,
                isGuest = user.isGuest,
                provider = if (user.isGuest) null else user.provider.name,
                email = user.email,
                createdAt = user.createdAt.toString(),
                settings = settings?.toResponse(),
            )
    }
}

/**
 * 사용자 설정 응답
 */
@Serializable
data class UserSettingsResponse(
    val notificationEnabled: Boolean,
    val notificationTime: String,
    val dDayAlerts: List<Int>,
)

fun User.toResponse(settings: UserSettings? = null): UserResponse = UserResponse.from(this, settings)

fun UserSettings.toResponse(): UserSettingsResponse =
    UserSettingsResponse(
        notificationEnabled = notificationEnabled,
        notificationTime = notificationTime,
        dDayAlerts = dDayAlerts,
    )
