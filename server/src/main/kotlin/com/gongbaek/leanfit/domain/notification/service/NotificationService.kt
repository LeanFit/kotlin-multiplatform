package com.gongbaek.leanfit.domain.notification.service

import com.gongbaek.leanfit.domain.notification.dto.request.RegisterFcmTokenRequest
import com.gongbaek.leanfit.domain.notification.dto.response.NotificationListResponse
import com.gongbaek.leanfit.domain.notification.dto.response.toListResponse
import com.gongbaek.leanfit.domain.notification.entity.NotificationType
import com.gongbaek.leanfit.domain.notification.entity.Platform
import com.gongbaek.leanfit.domain.notification.repository.FcmTokenRepository
import com.gongbaek.leanfit.domain.notification.repository.NotificationRepository
import java.util.UUID

class NotificationService(
    private val notificationRepository: NotificationRepository = NotificationRepository(),
    private val fcmTokenRepository: FcmTokenRepository = FcmTokenRepository(),
) {
    companion object {
        private const val DEFAULT_PAGE_SIZE: Int = 20
    }

    /**
     * FCM 토큰 등록
     * POST /notifications/fcm-token
     */
    fun registerFcmToken(
        userId: UUID,
        request: RegisterFcmTokenRequest,
    ) {
        val platform = Platform.valueOf(request.platform)
        fcmTokenRepository.saveOrUpdate(userId, request.fcmToken, platform)
    }

    /**
     * 알림 목록 조회
     * GET /notifications
     */
    fun getNotifications(
        userId: UUID,
        limit: Int?,
        cursor: String?,
    ): NotificationListResponse {
        val pageSize = limit ?: DEFAULT_PAGE_SIZE
        val cursorUUID = cursor?.let { UUID.fromString(it) }

        // limit + 1 개 조회해서 다음 페이지 존재 여부 확인
        val notifications = notificationRepository.findByUserId(userId, pageSize + 1, cursorUUID)

        val hasMore = notifications.size > pageSize
        val resultNotifications = if (hasMore) notifications.dropLast(1) else notifications
        val nextCursor = if (hasMore) resultNotifications.lastOrNull()?.id?.toString() else null

        return resultNotifications.toListResponse(nextCursor, hasMore)
    }

    /**
     * 알림 읽음 처리
     */
    fun markAsRead(
        notificationId: UUID,
        userId: UUID,
    ) {
        notificationRepository.markAsRead(notificationId, userId)
    }

    /**
     * 모든 알림 읽음 처리
     */
    fun markAllAsRead(userId: UUID) {
        notificationRepository.markAllAsRead(userId)
    }

    /**
     * 결제 리마인더 알림 생성 (시스템 내부 호출용)
     */
    fun createPaymentReminder(
        userId: UUID,
        subscriptionName: String,
        amount: Double,
        paymentDate: String,
        dDay: Int,
        subscriptionId: UUID,
    ) {
        val title = if (dDay == 0) "오늘 결제 예정이에요" else "${dDay}일 후 결제 예정이에요"
        val body = "$subscriptionName ₩${amount.toInt().formatWithCommas()}이 ${paymentDate}에 결제돼요"

        notificationRepository.save(
            userId = userId,
            type = if (dDay == 0) NotificationType.PAYMENT_DUE else NotificationType.PAYMENT_REMINDER,
            title = title,
            body = body,
            subscriptionId = subscriptionId,
        )
    }

    private fun Int.formatWithCommas(): String = String.format("%,d", this)
}
