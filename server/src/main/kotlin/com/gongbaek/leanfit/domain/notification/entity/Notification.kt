package com.gongbaek.leanfit.domain.notification.entity

import kotlinx.datetime.Instant
import java.util.UUID

/**
 * 알림 타입
 */
enum class NotificationType {
    PAYMENT_REMINDER, // 결제 리마인더
    PAYMENT_DUE, // 결제일 당일
    SUBSCRIPTION_ADDED, // 구독 추가 완료
    SYSTEM, // 시스템 알림
}

/**
 * 알림 엔티티
 */
data class Notification(
    val id: UUID,
    val userId: UUID,
    val type: NotificationType,
    val title: String,
    val body: String,
    val subscriptionId: UUID?,
    val isRead: Boolean,
    val createdAt: Instant,
)

/**
 * 플랫폼 타입
 */
enum class Platform {
    ANDROID,
    IOS,
}

/**
 * FCM 토큰 엔티티
 */
data class FcmToken(
    val id: UUID,
    val userId: UUID,
    val token: String,
    val platform: Platform,
    val createdAt: Instant,
    val updatedAt: Instant,
)
