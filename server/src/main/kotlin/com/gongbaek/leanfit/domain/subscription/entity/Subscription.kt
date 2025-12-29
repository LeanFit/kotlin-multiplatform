package com.gongbaek.leanfit.domain.subscription.entity

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.util.UUID

/**
 * 구독 상태
 */
enum class SubscriptionStatus {
    ACTIVE,
    PAUSED,
    CANCELLED,
}

/**
 * 결제 주기
 */
enum class BillingCycle {
    MONTHLY,
    YEARLY,
    WEEKLY,
}

/**
 * 구독 카테고리
 */
enum class SubscriptionCategory {
    ENTERTAINMENT,
    PRODUCTIVITY,
    LIFESTYLE,
    GAMING,
    NEWS,
    FINANCE,
    EDUCATION,
    OTHER,
}

/**
 * 구독 엔티티
 * - 낙관적 잠금을 위한 version 필드 포함
 */
data class Subscription(
    val id: UUID,
    val userId: UUID,
    val serviceName: String,
    val amount: BigDecimal,
    val currency: String,
    val billingCycle: BillingCycle,
    val billingDay: Int,
    val category: SubscriptionCategory?,
    val iconUrl: String?,
    val status: SubscriptionStatus,
    val memo: String?,
    val startDate: LocalDate?,
    val nextPaymentDate: LocalDate?,
    val version: Long = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
)
