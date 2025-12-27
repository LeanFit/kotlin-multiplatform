package com.gongbaek.garangbi.infrastructure.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object SubscriptionsTable : UUIDTable("subscriptions") {
    val userId = reference("user_id", UsersTable)
    val serviceName = varchar("service_name", 100)
    val amount = integer("amount")
    val currency = varchar("currency", 3).default("KRW")
    val billingCycle = varchar("billing_cycle", 20).default("MONTHLY")
    val billingDay = integer("billing_day")
    val category = varchar("category", 50).nullable()
    val iconUrl = varchar("icon_url", 500).nullable()
    val memo = text("memo").nullable()
    val isActive = bool("is_active").default(true)
    val startDate = timestamp("start_date").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
