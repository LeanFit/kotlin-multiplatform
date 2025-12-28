package com.gongbaek.leanfit.domain.subscription.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateSubscriptionRequest(
    val serviceName: String,
    val amount: Int,
    val currency: String = "KRW",
    val billingCycle: String = "MONTHLY",
    val billingDay: Int,
    val category: String? = null,
    val iconUrl: String? = null,
    val memo: String? = null,
    val startDate: String? = null,
) {
    init {
        require(serviceName.isNotBlank()) { "serviceName must not be blank" }
        require(amount > 0) { "amount must be positive" }
        require(billingDay in 1..31) { "billingDay must be between 1 and 31" }
    }
}

@Serializable
data class UpdateSubscriptionRequest(
    val serviceName: String,
    val amount: Int,
    val currency: String = "KRW",
    val billingCycle: String = "MONTHLY",
    val billingDay: Int,
    val category: String? = null,
    val iconUrl: String? = null,
    val memo: String? = null,
    val startDate: String? = null,
    val isActive: Boolean = true,
) {
    init {
        require(serviceName.isNotBlank()) { "serviceName must not be blank" }
        require(amount > 0) { "amount must be positive" }
        require(billingDay in 1..31) { "billingDay must be between 1 and 31" }
    }
}
