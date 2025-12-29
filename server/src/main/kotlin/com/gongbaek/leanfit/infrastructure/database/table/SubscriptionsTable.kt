package com.gongbaek.leanfit.infrastructure.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * 구독 테이블
 * - 낙관적 잠금을 위한 version 필드 포함
 */
object SubscriptionsTable : UUIDTable("subscriptions") {
    val userId = reference("user_id", UsersTable)
    val serviceName = varchar("service_name", 100)
    val amount = decimal("amount", 10, 2)
    val currency = varchar("currency", 3).default("KRW")
    val billingCycle = varchar("billing_cycle", 20).default("MONTHLY")
    val billingDay = integer("billing_day")
    val category = varchar("category", 50).nullable()
    val iconUrl = varchar("icon_url", 500).nullable()
    val status = varchar("status", 20).default("ACTIVE")
    val memo = text("memo").nullable()
    val startDate = date("start_date").nullable()
    val nextPaymentDate = date("next_payment_date").nullable()
    val version = long("version").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

/**
 * 결제 내역 테이블
 */
object PaymentHistoryTable : UUIDTable("payment_history") {
    val subscriptionId = reference("subscription_id", SubscriptionsTable)
    val amount = decimal("amount", 10, 2)
    val paymentDate = date("payment_date")
    val status = varchar("status", 20).default("PAID")
    val createdAt = timestamp("created_at")
}

/**
 * 알림 테이블
 */
object NotificationsTable : UUIDTable("notifications") {
    val userId = reference("user_id", UsersTable)
    val type = varchar("type", 50)
    val title = varchar("title", 100)
    val body = varchar("body", 500)
    val subscriptionId = uuid("subscription_id").nullable()
    val isRead = bool("is_read").default(false)
    val createdAt = timestamp("created_at")
}
