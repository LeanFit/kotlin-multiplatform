package com.gongbaek.leanfit.infrastructure.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * 사용자 테이블
 * - 게스트 인증 (deviceId 기반)
 * - 소셜 로그인 (Google, Kakao, Apple)
 * - 낙관적 잠금을 위한 version 필드 포함
 */
object UsersTable : UUIDTable("users") {
    val deviceId = varchar("device_id", 128).nullable().uniqueIndex()
    val provider = varchar("provider", 20).default("GUEST")
    val providerId = varchar("provider_id", 255).nullable()
    val email = varchar("email", 255).nullable()
    val nickname = varchar("nickname", 20).nullable()
    val isGuest = bool("is_guest").default(true)
    val version = long("version").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
}

/**
 * 사용자 설정 테이블
 */
object UserSettingsTable : UUIDTable("user_settings") {
    val userId = reference("user_id", UsersTable).uniqueIndex()
    val notificationEnabled = bool("notification_enabled").default(true)
    val notificationTime = varchar("notification_time", 5).default("09:00")
    val dDayAlerts = varchar("d_day_alerts", 50).default("3,1")
    val updatedAt = timestamp("updated_at")
}

/**
 * 리프레시 토큰 테이블
 */
object RefreshTokensTable : UUIDTable("refresh_tokens") {
    val userId = reference("user_id", UsersTable)
    val tokenHash = varchar("token_hash", 255)
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
}

/**
 * FCM 토큰 테이블
 */
object FcmTokensTable : UUIDTable("fcm_tokens") {
    val userId = reference("user_id", UsersTable)
    val token = varchar("token", 500)
    val platform = varchar("platform", 10)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

/**
 * 멱등성 키 테이블
 * - POST/PUT/DELETE 요청의 멱등성 보장
 * - 동일한 요청이 중복으로 들어올 경우 캐싱된 응답 반환
 */
object IdempotencyKeysTable : UUIDTable("idempotency_keys") {
    val key = varchar("key", 64).uniqueIndex()
    val userId = reference("user_id", UsersTable)
    val requestPath = varchar("request_path", 255)
    val requestMethod = varchar("request_method", 10)
    val responseStatus = integer("response_status")
    val responseBody = text("response_body")
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")

    init {
        index(isUnique = true, key, userId)
    }
}
