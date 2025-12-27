package com.gongbaek.garangbi.domain.subscription.dto.response

import com.gongbaek.garangbi.domain.subscription.entity.Subscription
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionResponse(
    val id: String,
    val serviceName: String,
    val amount: Int,
    val currency: String,
    val billingCycle: String,
    val billingDay: Int,
    val category: String?,
    val iconUrl: String?,
    val memo: String?,
    val isActive: Boolean,
    val startDate: String?,
    val createdAt: String,
) {
    companion object {
        fun from(subscription: Subscription): SubscriptionResponse =
            SubscriptionResponse(
                id = subscription.id.toString(),
                serviceName = subscription.serviceName,
                amount = subscription.amount,
                currency = subscription.currency,
                billingCycle = subscription.billingCycle,
                billingDay = subscription.billingDay,
                category = subscription.category,
                iconUrl = subscription.iconUrl,
                memo = subscription.memo,
                isActive = subscription.isActive,
                startDate = subscription.startDate?.toString(),
                createdAt = subscription.createdAt.toString(),
            )
    }
}

fun Subscription.toResponse(): SubscriptionResponse = SubscriptionResponse.from(this)
