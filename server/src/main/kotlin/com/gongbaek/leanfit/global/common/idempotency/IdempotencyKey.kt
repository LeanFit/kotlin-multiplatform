package com.gongbaek.leanfit.global.common.idempotency

import kotlinx.datetime.Instant
import java.util.UUID

/**
 * 멱등성 키 엔티티
 */
data class IdempotencyKey(
    val id: UUID,
    val key: String,
    val userId: UUID,
    val requestPath: String,
    val requestMethod: String,
    val responseStatus: Int,
    val responseBody: String,
    val expiresAt: Instant,
    val createdAt: Instant,
)

/**
 * 저장용 멱등성 키 데이터
 */
data class IdempotencyKeyData(
    val key: String,
    val userId: UUID,
    val requestPath: String,
    val requestMethod: String,
    val responseStatus: Int,
    val responseBody: String,
)
