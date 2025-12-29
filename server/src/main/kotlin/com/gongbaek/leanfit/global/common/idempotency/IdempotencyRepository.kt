package com.gongbaek.leanfit.global.common.idempotency

import com.gongbaek.leanfit.infrastructure.database.table.IdempotencyKeysTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.time.Duration.Companion.hours

/**
 * 멱등성 키 저장소
 * - 기본 만료 시간: 24시간
 */
class IdempotencyRepository {
    companion object {
        private val DEFAULT_EXPIRATION = 24.hours
    }

    /**
     * 멱등성 키 조회
     */
    fun findByKeyAndUserId(
        key: String,
        userId: UUID,
    ): IdempotencyKey? =
        transaction {
            val now = Clock.System.now()
            IdempotencyKeysTable
                .selectAll()
                .where {
                    (IdempotencyKeysTable.key eq key) and
                        (IdempotencyKeysTable.userId eq userId) and
                        (IdempotencyKeysTable.expiresAt greater now)
                }
                .singleOrNull()
                ?.toIdempotencyKey()
        }

    /**
     * 멱등성 키 저장
     */
    fun save(data: IdempotencyKeyData): IdempotencyKey =
        transaction {
            val now = Clock.System.now()
            val expiresAt = now + DEFAULT_EXPIRATION

            val id =
                IdempotencyKeysTable.insert {
                    it[key] = data.key
                    it[userId] = data.userId
                    it[requestPath] = data.requestPath
                    it[requestMethod] = data.requestMethod
                    it[responseStatus] = data.responseStatus
                    it[responseBody] = data.responseBody
                    it[IdempotencyKeysTable.expiresAt] = expiresAt
                    it[createdAt] = now
                } get IdempotencyKeysTable.id

            IdempotencyKey(
                id = id.value,
                key = data.key,
                userId = data.userId,
                requestPath = data.requestPath,
                requestMethod = data.requestMethod,
                responseStatus = data.responseStatus,
                responseBody = data.responseBody,
                expiresAt = expiresAt,
                createdAt = now,
            )
        }

    /**
     * 만료된 멱등성 키 정리 (정기 작업용)
     */
    fun cleanupExpired(): Int =
        transaction {
            val now = Clock.System.now()
            IdempotencyKeysTable.deleteWhere {
                expiresAt less now
            }
        }

    private fun ResultRow.toIdempotencyKey(): IdempotencyKey =
        IdempotencyKey(
            id = this[IdempotencyKeysTable.id].value,
            key = this[IdempotencyKeysTable.key],
            userId = this[IdempotencyKeysTable.userId].value,
            requestPath = this[IdempotencyKeysTable.requestPath],
            requestMethod = this[IdempotencyKeysTable.requestMethod],
            responseStatus = this[IdempotencyKeysTable.responseStatus],
            responseBody = this[IdempotencyKeysTable.responseBody],
            expiresAt = this[IdempotencyKeysTable.expiresAt],
            createdAt = this[IdempotencyKeysTable.createdAt],
        )
}
