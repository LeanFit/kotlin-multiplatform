package com.gongbaek.leanfit.domain.notification.repository

import com.gongbaek.leanfit.domain.notification.entity.FcmToken
import com.gongbaek.leanfit.domain.notification.entity.Notification
import com.gongbaek.leanfit.domain.notification.entity.NotificationType
import com.gongbaek.leanfit.domain.notification.entity.Platform
import com.gongbaek.leanfit.infrastructure.database.table.FcmTokensTable
import com.gongbaek.leanfit.infrastructure.database.table.NotificationsTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class NotificationRepository {
    /**
     * 알림 목록 조회 (커서 기반 페이지네이션)
     */
    fun findByUserId(
        userId: UUID,
        limit: Int,
        cursor: UUID?,
    ): List<Notification> =
        transaction {
            NotificationsTable
                .selectAll()
                .where {
                    if (cursor != null) {
                        (NotificationsTable.userId eq userId) and (NotificationsTable.id less cursor)
                    } else {
                        NotificationsTable.userId eq userId
                    }
                }.orderBy(NotificationsTable.createdAt, SortOrder.DESC)
                .limit(limit)
                .map { it.toNotification() }
        }

    /**
     * 알림 저장
     */
    fun save(
        userId: UUID,
        type: NotificationType,
        title: String,
        body: String,
        subscriptionId: UUID? = null,
    ): Notification =
        transaction {
            val now = Clock.System.now()
            val id =
                NotificationsTable.insert {
                    it[NotificationsTable.userId] = userId
                    it[NotificationsTable.type] = type.name
                    it[NotificationsTable.title] = title
                    it[NotificationsTable.body] = body
                    it[NotificationsTable.subscriptionId] = subscriptionId
                    it[isRead] = false
                    it[createdAt] = now
                } get NotificationsTable.id

            Notification(
                id = id.value,
                userId = userId,
                type = type,
                title = title,
                body = body,
                subscriptionId = subscriptionId,
                isRead = false,
                createdAt = now,
            )
        }

    /**
     * 알림 읽음 처리
     */
    fun markAsRead(
        id: UUID,
        userId: UUID,
    ): Int =
        transaction {
            NotificationsTable.update({
                (NotificationsTable.id eq id) and (NotificationsTable.userId eq userId)
            }) {
                it[isRead] = true
            }
        }

    /**
     * 모든 알림 읽음 처리
     */
    fun markAllAsRead(userId: UUID): Int =
        transaction {
            NotificationsTable.update({
                (NotificationsTable.userId eq userId) and (NotificationsTable.isRead eq false)
            }) {
                it[isRead] = true
            }
        }

    private fun ResultRow.toNotification(): Notification =
        Notification(
            id = this[NotificationsTable.id].value,
            userId = this[NotificationsTable.userId].value,
            type = NotificationType.valueOf(this[NotificationsTable.type]),
            title = this[NotificationsTable.title],
            body = this[NotificationsTable.body],
            subscriptionId = this[NotificationsTable.subscriptionId],
            isRead = this[NotificationsTable.isRead],
            createdAt = this[NotificationsTable.createdAt],
        )
}

class FcmTokenRepository {
    /**
     * FCM 토큰 등록/갱신 (upsert)
     */
    fun saveOrUpdate(
        userId: UUID,
        token: String,
        platform: Platform,
    ): FcmToken =
        transaction {
            val now = Clock.System.now()

            // 기존 토큰 조회 (같은 사용자의 같은 플랫폼)
            val existing =
                FcmTokensTable
                    .selectAll()
                    .where {
                        (FcmTokensTable.userId eq userId) and (FcmTokensTable.platform eq platform.name)
                    }.singleOrNull()

            if (existing != null) {
                // 업데이트
                FcmTokensTable.update({
                    (FcmTokensTable.userId eq userId) and (FcmTokensTable.platform eq platform.name)
                }) {
                    it[FcmTokensTable.token] = token
                    it[updatedAt] = now
                }

                FcmToken(
                    id = existing[FcmTokensTable.id].value,
                    userId = userId,
                    token = token,
                    platform = platform,
                    createdAt = existing[FcmTokensTable.createdAt],
                    updatedAt = now,
                )
            } else {
                // 새로 생성
                val id =
                    FcmTokensTable.insert {
                        it[FcmTokensTable.userId] = userId
                        it[FcmTokensTable.token] = token
                        it[FcmTokensTable.platform] = platform.name
                        it[createdAt] = now
                        it[updatedAt] = now
                    } get FcmTokensTable.id

                FcmToken(
                    id = id.value,
                    userId = userId,
                    token = token,
                    platform = platform,
                    createdAt = now,
                    updatedAt = now,
                )
            }
        }

    /**
     * 사용자의 FCM 토큰 조회
     */
    fun findByUserId(userId: UUID): List<FcmToken> =
        transaction {
            FcmTokensTable
                .selectAll()
                .where { FcmTokensTable.userId eq userId }
                .map { it.toFcmToken() }
        }

    /**
     * 토큰 삭제
     */
    fun deleteByToken(token: String): Int =
        transaction {
            FcmTokensTable.deleteWhere { FcmTokensTable.token eq token }
        }

    /**
     * 사용자의 모든 토큰 삭제
     */
    fun deleteByUserId(userId: UUID): Int =
        transaction {
            FcmTokensTable.deleteWhere { FcmTokensTable.userId eq userId }
        }

    private fun ResultRow.toFcmToken(): FcmToken =
        FcmToken(
            id = this[FcmTokensTable.id].value,
            userId = this[FcmTokensTable.userId].value,
            token = this[FcmTokensTable.token],
            platform = Platform.valueOf(this[FcmTokensTable.platform]),
            createdAt = this[FcmTokensTable.createdAt],
            updatedAt = this[FcmTokensTable.updatedAt],
        )
}
