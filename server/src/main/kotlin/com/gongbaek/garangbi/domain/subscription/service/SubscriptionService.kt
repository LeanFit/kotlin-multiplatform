package com.gongbaek.garangbi.domain.subscription.service

import com.gongbaek.garangbi.domain.subscription.dto.request.CreateSubscriptionRequest
import com.gongbaek.garangbi.domain.subscription.dto.request.UpdateSubscriptionRequest
import com.gongbaek.garangbi.domain.subscription.dto.response.SubscriptionResponse
import com.gongbaek.garangbi.domain.subscription.dto.response.toResponse
import com.gongbaek.garangbi.domain.subscription.repository.SubscriptionRepository
import com.gongbaek.garangbi.global.exception.BusinessException
import com.gongbaek.garangbi.global.exception.ErrorCode
import kotlinx.datetime.Instant
import java.util.UUID

class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepository(),
) {
    fun getSubscriptions(userId: UUID): List<SubscriptionResponse> =
        subscriptionRepository
            .findAllByUserId(userId)
            .map { it.toResponse() }

    fun createSubscription(
        userId: UUID,
        request: CreateSubscriptionRequest,
    ): SubscriptionResponse {
        val subscription =
            subscriptionRepository.save(
                userId = userId,
                serviceName = request.serviceName,
                amount = request.amount,
                currency = request.currency,
                billingCycle = request.billingCycle,
                billingDay = request.billingDay,
                category = request.category,
                iconUrl = request.iconUrl,
                memo = request.memo,
                startDate = request.startDate?.let { Instant.parse(it) },
            )
        return subscription.toResponse()
    }

    fun updateSubscription(
        subscriptionId: UUID,
        userId: UUID,
        request: UpdateSubscriptionRequest,
    ): SubscriptionResponse {
        validateSubscriptionAccess(subscriptionId, userId)

        val updatedCount =
            subscriptionRepository.update(
                id = subscriptionId,
                userId = userId,
                serviceName = request.serviceName,
                amount = request.amount,
                currency = request.currency,
                billingCycle = request.billingCycle,
                billingDay = request.billingDay,
                category = request.category,
                iconUrl = request.iconUrl,
                memo = request.memo,
                startDate = request.startDate?.let { Instant.parse(it) },
                isActive = request.isActive,
            )

        if (updatedCount == 0) {
            throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
        }

        return subscriptionRepository.findById(subscriptionId)?.toResponse()
            ?: throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
    }

    fun deleteSubscription(
        subscriptionId: UUID,
        userId: UUID,
    ) {
        validateSubscriptionAccess(subscriptionId, userId)

        val deletedCount = subscriptionRepository.deleteByIdAndUserId(subscriptionId, userId)

        if (deletedCount == 0) {
            throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
        }
    }

    private fun validateSubscriptionAccess(
        subscriptionId: UUID,
        userId: UUID,
    ) {
        subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
            ?: throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
    }
}
