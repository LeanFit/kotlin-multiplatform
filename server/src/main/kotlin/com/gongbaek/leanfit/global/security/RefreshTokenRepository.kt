package com.gongbaek.leanfit.global.security

import com.gongbaek.leanfit.infrastructure.database.table.RefreshTokensTable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.time.Duration.Companion.days

/**
 * Refresh Token 엔티티
 */
data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    val createdAt: Instant,
)

/**
 * Refresh Token 저장소
 * - 토큰 저장, 검증, 무효화 기능 제공
 */
class RefreshTokenRepository {
    companion object {
        private val TOKEN_EXPIRATION = 30.days
    }

    /**
     * Refresh Token 저장
     */
    fun save(
        userId: UUID,
        tokenHash: String,
    ): RefreshToken =
        transaction {
            val now = Clock.System.now()
            val expiresAt = now + TOKEN_EXPIRATION

            val id =
                RefreshTokensTable.insert {
                    it[RefreshTokensTable.userId] = userId
                    it[RefreshTokensTable.tokenHash] = tokenHash
                    it[RefreshTokensTable.expiresAt] = expiresAt
                    it[createdAt] = now
                } get RefreshTokensTable.id

            RefreshToken(
                id = id.value,
                userId = userId,
                tokenHash = tokenHash,
                expiresAt = expiresAt,
                createdAt = now,
            )
        }

    /**
     * 유효한 Refresh Token 조회
     */
    fun findValidToken(
        userId: UUID,
        tokenHash: String,
    ): RefreshToken? =
        transaction {
            val now = Clock.System.now()
            RefreshTokensTable
                .selectAll()
                .where {
                    (RefreshTokensTable.userId eq userId) and
                        (RefreshTokensTable.tokenHash eq tokenHash) and
                        (RefreshTokensTable.expiresAt greater now)
                }
                .singleOrNull()
                ?.let {
                    RefreshToken(
                        id = it[RefreshTokensTable.id].value,
                        userId = it[RefreshTokensTable.userId].value,
                        tokenHash = it[RefreshTokensTable.tokenHash],
                        expiresAt = it[RefreshTokensTable.expiresAt],
                        createdAt = it[RefreshTokensTable.createdAt],
                    )
                }
        }

    /**
     * 특정 토큰 무효화 (로그아웃)
     */
    fun invalidateToken(
        userId: UUID,
        tokenHash: String,
    ): Int =
        transaction {
            RefreshTokensTable.deleteWhere {
                (RefreshTokensTable.userId eq userId) and
                    (RefreshTokensTable.tokenHash eq tokenHash)
            }
        }

    /**
     * 사용자의 모든 토큰 무효화 (보안 이슈 또는 탈퇴 시)
     */
    fun invalidateAllTokens(userId: UUID): Int =
        transaction {
            RefreshTokensTable.deleteWhere {
                RefreshTokensTable.userId eq userId
            }
        }

    /**
     * 만료된 토큰 정리 (정기 작업용)
     */
    fun cleanupExpired(): Int =
        transaction {
            val now = Clock.System.now()
            RefreshTokensTable.deleteWhere {
                expiresAt less now
            }
        }
}
