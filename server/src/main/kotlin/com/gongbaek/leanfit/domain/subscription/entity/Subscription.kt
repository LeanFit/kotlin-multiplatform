package com.gongbaek.leanfit.domain.subscription.entity

import kotlinx.datetime.Instant
import java.util.UUID

data class Subscription(
    val id: UUID,
    val userId: UUID,
    val serviceName: String,
    val amount: Int,
    val currency: String,
    val billingCycle: String,
    val billingDay: Int,
    val category: String?,
    val iconUrl: String?,
    val memo: String?,
    val isActive: Boolean,
    val startDate: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
