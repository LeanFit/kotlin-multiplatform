package com.gongbaek.leanfit.domain.user.entity

import kotlinx.datetime.Instant
import java.util.UUID

/**
 * 인증 제공자 타입
 */
enum class AuthProvider {
    GUEST,
    GOOGLE,
    KAKAO,
    APPLE,
}

/**
 * 사용자 엔티티
 * - 낙관적 잠금을 위한 version 필드 포함
 */
data class User(
    val id: UUID,
    val deviceId: String?,
    val provider: AuthProvider,
    val providerId: String?,
    val email: String?,
    val nickname: String?,
    val isGuest: Boolean,
    val version: Long = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
)

/**
 * 사용자 설정 엔티티
 */
data class UserSettings(
    val userId: UUID,
    val notificationEnabled: Boolean = true,
    val notificationTime: String = "09:00",
    val dDayAlerts: List<Int> = listOf(3, 1),
    val updatedAt: Instant,
)
