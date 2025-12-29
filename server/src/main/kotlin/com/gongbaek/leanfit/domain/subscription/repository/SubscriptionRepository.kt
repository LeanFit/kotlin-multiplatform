package com.gongbaek.leanfit.domain.subscription.repository

import com.gongbaek.leanfit.domain.subscription.entity.BillingCycle
import com.gongbaek.leanfit.domain.subscription.entity.Subscription
import com.gongbaek.leanfit.domain.subscription.entity.SubscriptionCategory
import com.gongbaek.leanfit.domain.subscription.entity.SubscriptionStatus
import com.gongbaek.leanfit.global.exception.BusinessException
import com.gongbaek.leanfit.global.exception.ErrorCode
import com.gongbaek.leanfit.infrastructure.database.table.SubscriptionsTable
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.util.UUID

class SubscriptionRepository {
    fun findAllByUserId(
        userId: UUID,
        status: SubscriptionStatus? = null,
    ): List<Subscription> =
        transaction {
            SubscriptionsTable
                .selectAll()
                .where {
                    if (status != null) {
                        (SubscriptionsTable.userId eq userId) and (SubscriptionsTable.status eq status.name)
                    } else {
                        SubscriptionsTable.userId eq userId
                    }
                }
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
        amount: BigDecimal,
        currency: String,
        billingCycle: BillingCycle,
        billingDay: Int,
        category: SubscriptionCategory?,
        memo: String?,
        startDate: LocalDate?,
        nextPaymentDate: LocalDate?,
    ): Subscription =
        transaction {
            val now = Clock.System.now()
            val initialVersion = 0L
            val id =
                SubscriptionsTable.insert {
                    it[SubscriptionsTable.userId] = userId
                    it[SubscriptionsTable.serviceName] = serviceName
                    it[SubscriptionsTable.amount] = amount
                    it[SubscriptionsTable.currency] = currency
                    it[SubscriptionsTable.billingCycle] = billingCycle.name
                    it[SubscriptionsTable.billingDay] = billingDay
                    it[SubscriptionsTable.category] = category?.name
                    it[status] = SubscriptionStatus.ACTIVE.name
                    it[SubscriptionsTable.memo] = memo
                    it[SubscriptionsTable.startDate] = startDate
                    it[SubscriptionsTable.nextPaymentDate] = nextPaymentDate
                    it[version] = initialVersion
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
                iconUrl = null,
                status = SubscriptionStatus.ACTIVE,
                memo = memo,
                startDate = startDate,
                nextPaymentDate = nextPaymentDate,
                version = initialVersion,
                createdAt = now,
                updatedAt = now,
            )
        }

    /**
     * 구독 업데이트 (낙관적 잠금 적용)
     * @throws BusinessException CONCURRENT_MODIFICATION 동시성 충돌 시
     */
    fun update(
        id: UUID,
        userId: UUID,
        serviceName: String,
        amount: BigDecimal,
        currency: String,
        billingCycle: BillingCycle,
        billingDay: Int,
        category: SubscriptionCategory?,
        memo: String?,
        expectedVersion: Long,
    ): Subscription =
        transaction {
            val now = Clock.System.now()
            val updatedCount =
                SubscriptionsTable.update({
                    (SubscriptionsTable.id eq id) and
                        (SubscriptionsTable.userId eq userId) and
                        (SubscriptionsTable.version eq expectedVersion)
                }) {
                    it[SubscriptionsTable.serviceName] = serviceName
                    it[SubscriptionsTable.amount] = amount
                    it[SubscriptionsTable.currency] = currency
                    it[SubscriptionsTable.billingCycle] = billingCycle.name
                    it[SubscriptionsTable.billingDay] = billingDay
                    it[SubscriptionsTable.category] = category?.name
                    it[SubscriptionsTable.memo] = memo
                    it[version] = expectedVersion + 1
                    it[updatedAt] = now
                }

            if (updatedCount == 0) {
                throw BusinessException(ErrorCode.CONCURRENT_MODIFICATION)
            }

            findById(id) ?: throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
        }

    /**
     * 상태 업데이트 (낙관적 잠금 적용)
     * @throws BusinessException CONCURRENT_MODIFICATION 동시성 충돌 시
     */
    fun updateStatus(
        id: UUID,
        userId: UUID,
        status: SubscriptionStatus,
        expectedVersion: Long,
    ): Subscription =
        transaction {
            val now = Clock.System.now()
            val updatedCount =
                SubscriptionsTable.update({
                    (SubscriptionsTable.id eq id) and
                        (SubscriptionsTable.userId eq userId) and
                        (SubscriptionsTable.version eq expectedVersion)
                }) {
                    it[SubscriptionsTable.status] = status.name
                    it[version] = expectedVersion + 1
                    it[updatedAt] = now
                }

            if (updatedCount == 0) {
                throw BusinessException(ErrorCode.CONCURRENT_MODIFICATION)
            }

            findById(id) ?: throw BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
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

    fun deleteAllByUserId(userId: UUID): Int =
        transaction {
            SubscriptionsTable.deleteWhere {
                SubscriptionsTable.userId eq userId
            }
        }

    /**
     * 사용자의 모든 구독을 새 사용자로 이전 (게스트→소셜 전환 시 사용)
     */
    fun transferSubscriptions(
        fromUserId: UUID,
        toUserId: UUID,
    ): Int =
        transaction {
            SubscriptionsTable.update({
                SubscriptionsTable.userId eq fromUserId
            }) {
                it[userId] = toUserId
                it[updatedAt] = Clock.System.now()
            }
        }

    private fun ResultRow.toSubscription(): Subscription =
        Subscription(
            id = this[SubscriptionsTable.id].value,
            userId = this[SubscriptionsTable.userId].value,
            serviceName = this[SubscriptionsTable.serviceName],
            amount = this[SubscriptionsTable.amount],
            currency = this[SubscriptionsTable.currency],
            billingCycle = BillingCycle.valueOf(this[SubscriptionsTable.billingCycle]),
            billingDay = this[SubscriptionsTable.billingDay],
            category = this[SubscriptionsTable.category]?.let { SubscriptionCategory.valueOf(it) },
            iconUrl = this[SubscriptionsTable.iconUrl],
            status = SubscriptionStatus.valueOf(this[SubscriptionsTable.status]),
            memo = this[SubscriptionsTable.memo],
            startDate = this[SubscriptionsTable.startDate],
            nextPaymentDate = this[SubscriptionsTable.nextPaymentDate],
            version = this[SubscriptionsTable.version],
            createdAt = this[SubscriptionsTable.createdAt],
            updatedAt = this[SubscriptionsTable.updatedAt],
        )
}
