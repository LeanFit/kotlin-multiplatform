package com.gongbaek.leanfit.domain.subscription.dto.response

import com.gongbaek.leanfit.domain.subscription.entity.Subscription
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

/**
 * 구독 응답
 */
@Serializable
data class SubscriptionResponse(
    val id: String,
    val name: String,
    val amount: Double,
    val currency: String,
    val paymentDay: Int,
    val cycle: String,
    val category: String?,
    val iconUrl: String?,
    val status: String,
    val memo: String?,
    val startDate: String?,
    val nextPaymentDate: String?,
    val dDay: Int?,
    val createdAt: String,
) {
    companion object {
        fun from(subscription: Subscription): SubscriptionResponse {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val dDay = subscription.nextPaymentDate?.let { calculateDDay(today, it) }

            return SubscriptionResponse(
                id = subscription.id.toString(),
                name = subscription.serviceName,
                amount = subscription.amount.toDouble(),
                currency = subscription.currency,
                paymentDay = subscription.billingDay,
                cycle = subscription.billingCycle.name,
                category = subscription.category?.name,
                iconUrl = subscription.iconUrl,
                status = subscription.status.name,
                memo = subscription.memo,
                startDate = subscription.startDate?.toString(),
                nextPaymentDate = subscription.nextPaymentDate?.toString(),
                dDay = dDay,
                createdAt = subscription.createdAt.toString(),
            )
        }

        private fun calculateDDay(
            today: LocalDate,
            nextPaymentDate: LocalDate,
        ): Int = today.daysUntil(nextPaymentDate)
    }
}

/**
 * 구독 목록 응답
 */
@Serializable
data class SubscriptionListResponse(
    val subscriptions: List<SubscriptionResponse>,
    val summary: SubscriptionSummary,
)

/**
 * 구독 요약 정보
 */
@Serializable
data class SubscriptionSummary(
    val totalCount: Int,
    val activeCount: Int,
    val totalMonthlyAmount: Double,
    val totalYearlyAmount: Double,
)

fun Subscription.toResponse(): SubscriptionResponse = SubscriptionResponse.from(this)

fun List<Subscription>.toListResponse(): SubscriptionListResponse {
    val activeSubscriptions = this.filter { it.status.name == "ACTIVE" }

    val totalMonthlyAmount =
        activeSubscriptions.sumOf {
            when (it.billingCycle.name) {
                "MONTHLY" -> it.amount.toDouble()
                "YEARLY" -> it.amount.toDouble() / 12
                "WEEKLY" -> it.amount.toDouble() * 4
                else -> it.amount.toDouble()
            }
        }

    return SubscriptionListResponse(
        subscriptions = this.map { it.toResponse() },
        summary =
            SubscriptionSummary(
                totalCount = this.size,
                activeCount = activeSubscriptions.size,
                totalMonthlyAmount = totalMonthlyAmount,
                totalYearlyAmount = totalMonthlyAmount * 12,
            ),
    )
}
