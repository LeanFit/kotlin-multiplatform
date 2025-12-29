package com.gongbaek.leanfit.domain.subscription.service

import com.gongbaek.leanfit.domain.subscription.dto.request.CreateSubscriptionRequest
import com.gongbaek.leanfit.domain.subscription.dto.request.UpdateStatusRequest
import com.gongbaek.leanfit.domain.subscription.dto.request.UpdateSubscriptionRequest
import com.gongbaek.leanfit.domain.subscription.dto.response.SubscriptionListResponse
import com.gongbaek.leanfit.domain.subscription.dto.response.SubscriptionResponse
import com.gongbaek.leanfit.domain.subscription.dto.response.toListResponse
import com.gongbaek.leanfit.domain.subscription.dto.response.toResponse
import com.gongbaek.leanfit.domain.subscription.entity.BillingCycle
import com.gongbaek.leanfit.domain.subscription.entity.Subscription
import com.gongbaek.leanfit.domain.subscription.entity.SubscriptionCategory
import com.gongbaek.leanfit.domain.subscription.entity.SubscriptionStatus
import com.gongbaek.leanfit.domain.subscription.repository.SubscriptionRepository
import com.gongbaek.leanfit.global.exception.BusinessException
import com.gongbaek.leanfit.global.exception.ErrorCode
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import java.math.BigDecimal
import java.util.UUID

class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepository(),
) {
    /**
     * 구독 목록 조회
     * GET /subscriptions
     */
    fun getSubscriptions(
        userId: UUID,
        status: String? = null,
    ): SubscriptionListResponse {
        val subscriptionStatus = status?.let { SubscriptionStatus.valueOf(it) }
        val subscriptions = subscriptionRepository.findAllByUserId(userId, subscriptionStatus)
        return subscriptions.toListResponse()
    }

    /**
     * 구독 상세 조회
     * GET /subscriptions/{id}
     */
    fun getSubscription(
        subscriptionId: UUID,
        userId: UUID,
    ): SubscriptionResponse {
        val subscription =
            subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                ?: throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
        return subscription.toResponse()
    }

    /**
     * 구독 추가
     * POST /subscriptions
     */
    fun createSubscription(
        userId: UUID,
        request: CreateSubscriptionRequest,
    ): SubscriptionResponse {
        val billingCycle = BillingCycle.valueOf(request.cycle)
        val category = request.category?.let { SubscriptionCategory.valueOf(it) }
        val startDate = request.startDate?.let { LocalDate.parse(it) }
        val nextPaymentDate = calculateNextPaymentDate(request.paymentDay)

        val subscription =
            subscriptionRepository.save(
                userId = userId,
                serviceName = request.name,
                amount = BigDecimal.valueOf(request.amount),
                currency = request.currency,
                billingCycle = billingCycle,
                billingDay = request.paymentDay,
                category = category,
                memo = request.memo,
                startDate = startDate,
                nextPaymentDate = nextPaymentDate,
            )
        return subscription.toResponse()
    }

    /**
     * 구독 수정 (낙관적 잠금 적용)
     * PUT /subscriptions/{id}
     */
    fun updateSubscription(
        subscriptionId: UUID,
        userId: UUID,
        request: UpdateSubscriptionRequest,
    ): SubscriptionResponse {
        val currentSubscription = validateSubscriptionAccess(subscriptionId, userId)

        val billingCycle = BillingCycle.valueOf(request.cycle)
        val category = request.category?.let { SubscriptionCategory.valueOf(it) }

        val updatedSubscription =
            subscriptionRepository.update(
                id = subscriptionId,
                userId = userId,
                serviceName = request.name,
                amount = BigDecimal.valueOf(request.amount),
                currency = request.currency,
                billingCycle = billingCycle,
                billingDay = request.paymentDay,
                category = category,
                memo = request.memo,
                expectedVersion = currentSubscription.version,
            )

        return updatedSubscription.toResponse()
    }

    /**
     * 구독 삭제
     * DELETE /subscriptions/{id}
     */
    fun deleteSubscription(
        subscriptionId: UUID,
        userId: UUID,
    ) {
        // 존재 여부 확인
        subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
            ?: throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)

        val deletedCount = subscriptionRepository.deleteByIdAndUserId(subscriptionId, userId)

        if (deletedCount == 0) {
            throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
        }
    }

    /**
     * 구독 상태 변경 (낙관적 잠금 적용)
     * PATCH /subscriptions/{id}/status
     */
    fun updateStatus(
        subscriptionId: UUID,
        userId: UUID,
        request: UpdateStatusRequest,
    ): SubscriptionResponse {
        val currentSubscription = validateSubscriptionAccess(subscriptionId, userId)

        val status = SubscriptionStatus.valueOf(request.status)
        val updatedSubscription =
            subscriptionRepository.updateStatus(
                id = subscriptionId,
                userId = userId,
                status = status,
                expectedVersion = currentSubscription.version,
            )

        return updatedSubscription.toResponse()
    }

    private fun validateSubscriptionAccess(
        subscriptionId: UUID,
        userId: UUID,
    ): Subscription =
        subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
            ?: throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)

    private fun calculateNextPaymentDate(paymentDay: Int): LocalDate {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val thisMonth =
            LocalDate(today.year, today.monthNumber, minOf(paymentDay, today.month.length(isLeapYear(today.year))))

        return if (thisMonth >= today) {
            thisMonth
        } else {
            val nextMonth = today.plus(1, DateTimeUnit.MONTH)
            LocalDate(
                nextMonth.year,
                nextMonth.monthNumber,
                minOf(paymentDay, nextMonth.month.length(isLeapYear(nextMonth.year))),
            )
        }
    }

    private fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}
