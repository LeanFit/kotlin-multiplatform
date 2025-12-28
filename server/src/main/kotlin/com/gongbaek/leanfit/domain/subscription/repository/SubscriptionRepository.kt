package com.gongbaek.leanfit.domain.subscription.repository

import com.gongbaek.leanfit.domain.subscription.entity.Subscription
import com.gongbaek.leanfit.infrastructure.database.table.SubscriptionsTable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class SubscriptionRepository {
    fun findAllByUserId(userId: UUID): List<Subscription> =
        transaction {
            SubscriptionsTable
                .selectAll()
                .where { SubscriptionsTable.userId eq userId }
                .map { it.toSubscription() }
        }

    fun findById(id: UUID): Subscription? =
        transaction {
            SubscriptionsTable
                .selectAll()
                .where { SubscriptionsTable.id eq id }
                .singleOrNull()
                ?.toSubscription()
        }

    fun findByIdAndUserId(
        id: UUID,
        userId: UUID,
    ): Subscription? =
        transaction {
            SubscriptionsTable
                .selectAll()
                .where { (SubscriptionsTable.id eq id) and (SubscriptionsTable.userId eq userId) }
                .singleOrNull()
                ?.toSubscription()
        }

    fun save(
        userId: UUID,
        serviceName: String,
        amount: Int,
        currency: String,
        billingCycle: String,
        billingDay: Int,
        category: String?,
        iconUrl: String?,
        memo: String?,
        startDate: Instant?,
    ): Subscription =
        transaction {
            val now = Clock.System.now()
            val id =
                SubscriptionsTable.insert {
                    it[SubscriptionsTable.userId] = userId
                    it[SubscriptionsTable.serviceName] = serviceName
                    it[SubscriptionsTable.amount] = amount
                    it[SubscriptionsTable.currency] = currency
                    it[SubscriptionsTable.billingCycle] = billingCycle
                    it[SubscriptionsTable.billingDay] = billingDay
                    it[SubscriptionsTable.category] = category
                    it[SubscriptionsTable.iconUrl] = iconUrl
                    it[SubscriptionsTable.memo] = memo
                    it[SubscriptionsTable.startDate] = startDate
                    it[isActive] = true
                    it[createdAt] = now
                    it[updatedAt] = now
                } get SubscriptionsTable.id

            Subscription(
                id = id.value,
                userId = userId,
                serviceName = serviceName,
                amount = amount,
                currency = currency,
                billingCycle = billingCycle,
                billingDay = billingDay,
                category = category,
                iconUrl = iconUrl,
                memo = memo,
                isActive = true,
                startDate = startDate,
                createdAt = now,
                updatedAt = now,
            )
        }

    fun update(
        id: UUID,
        userId: UUID,
        serviceName: String,
        amount: Int,
        currency: String,
        billingCycle: String,
        billingDay: Int,
        category: String?,
        iconUrl: String?,
        memo: String?,
        startDate: Instant?,
        isActive: Boolean,
    ): Int =
        transaction {
            SubscriptionsTable.update({
                (SubscriptionsTable.id eq id) and (SubscriptionsTable.userId eq userId)
            }) {
                it[SubscriptionsTable.serviceName] = serviceName
                it[SubscriptionsTable.amount] = amount
                it[SubscriptionsTable.currency] = currency
                it[SubscriptionsTable.billingCycle] = billingCycle
                it[SubscriptionsTable.billingDay] = billingDay
                it[SubscriptionsTable.category] = category
                it[SubscriptionsTable.iconUrl] = iconUrl
                it[SubscriptionsTable.memo] = memo
                it[SubscriptionsTable.startDate] = startDate
                it[SubscriptionsTable.isActive] = isActive
                it[updatedAt] = Clock.System.now()
            }
        }

    fun deleteByIdAndUserId(
        id: UUID,
        userId: UUID,
    ): Int =
        transaction {
            SubscriptionsTable.deleteWhere {
                (SubscriptionsTable.id eq id) and (SubscriptionsTable.userId eq userId)
            }
        }

    private fun ResultRow.toSubscription(): Subscription =
        Subscription(
            id = this[SubscriptionsTable.id].value,
            userId = this[SubscriptionsTable.userId].value,
            serviceName = this[SubscriptionsTable.serviceName],
            amount = this[SubscriptionsTable.amount],
            currency = this[SubscriptionsTable.currency],
            billingCycle = this[SubscriptionsTable.billingCycle],
            billingDay = this[SubscriptionsTable.billingDay],
            category = this[SubscriptionsTable.category],
            iconUrl = this[SubscriptionsTable.iconUrl],
            memo = this[SubscriptionsTable.memo],
            isActive = this[SubscriptionsTable.isActive],
            startDate = this[SubscriptionsTable.startDate],
            createdAt = this[SubscriptionsTable.createdAt],
            updatedAt = this[SubscriptionsTable.updatedAt],
        )
}
