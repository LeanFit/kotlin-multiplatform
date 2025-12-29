package com.gongbaek.leanfit.domain.subscription.dto.request

import kotlinx.serialization.Serializable

/**
 * 구독 추가 요청
 * POST /subscriptions
 */
@Serializable
data class CreateSubscriptionRequest(
    val name: String,
    val amount: Double,
    val currency: String = "KRW",
    val paymentDay: Int,
    val cycle: String = "MONTHLY",
    val category: String? = null,
    val memo: String? = null,
    val startDate: String? = null,
) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(name.length <= 50) { "name must be 50 characters or less" }
        require(amount >= 0) { "amount must be 0 or positive" }
        require(paymentDay in 1..31) { "paymentDay must be between 1 and 31" }
        require(cycle in listOf("MONTHLY", "YEARLY", "WEEKLY")) { "cycle must be MONTHLY, YEARLY, or WEEKLY" }
        memo?.let {
            require(it.length <= 200) { "memo must be 200 characters or less" }
        }
    }
}

/**
 * 구독 수정 요청
 * PUT /subscriptions/{id}
 */
@Serializable
data class UpdateSubscriptionRequest(
    val name: String,
    val amount: Double,
    val currency: String = "KRW",
    val paymentDay: Int,
    val cycle: String = "MONTHLY",
    val category: String? = null,
    val memo: String? = null,
) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(name.length <= 50) { "name must be 50 characters or less" }
        require(amount >= 0) { "amount must be 0 or positive" }
        require(paymentDay in 1..31) { "paymentDay must be between 1 and 31" }
        require(cycle in listOf("MONTHLY", "YEARLY", "WEEKLY")) { "cycle must be MONTHLY, YEARLY, or WEEKLY" }
        memo?.let {
            require(it.length <= 200) { "memo must be 200 characters or less" }
        }
    }
}

/**
 * 구독 상태 변경 요청
 * PATCH /subscriptions/{id}/status
 */
@Serializable
data class UpdateStatusRequest(
    val status: String,
) {
    init {
        require(status in listOf("ACTIVE", "PAUSED", "CANCELLED")) {
            "status must be ACTIVE, PAUSED, or CANCELLED"
        }
    }
}
