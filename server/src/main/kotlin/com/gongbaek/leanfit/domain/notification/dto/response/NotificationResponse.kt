package com.gongbaek.leanfit.domain.notification.dto.response

import com.gongbaek.leanfit.domain.notification.entity.Notification
import kotlinx.serialization.Serializable

/**
 * 알림 응답
 */
@Serializable
data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val subscriptionId: String?,
    val isRead: Boolean,
    val createdAt: String,
) {
    companion object {
        fun from(notification: Notification): NotificationResponse =
            NotificationResponse(
                id = notification.id.toString(),
                type = notification.type.name,
                title = notification.title,
                body = notification.body,
                subscriptionId = notification.subscriptionId?.toString(),
                isRead = notification.isRead,
                createdAt = notification.createdAt.toString(),
            )
    }
}

/**
 * 알림 목록 응답
 */
@Serializable
data class NotificationListResponse(
    val notifications: List<NotificationResponse>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

fun Notification.toResponse(): NotificationResponse = NotificationResponse.from(this)

fun List<Notification>.toListResponse(
    nextCursor: String?,
    hasMore: Boolean,
): NotificationListResponse =
    NotificationListResponse(
        notifications = this.map { it.toResponse() },
        nextCursor = nextCursor,
        hasMore = hasMore,
    )
