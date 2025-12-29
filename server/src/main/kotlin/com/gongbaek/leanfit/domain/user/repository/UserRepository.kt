package com.gongbaek.leanfit.domain.user.repository

import com.gongbaek.leanfit.domain.user.entity.AuthProvider
import com.gongbaek.leanfit.domain.user.entity.User
import com.gongbaek.leanfit.domain.user.entity.UserSettings
import com.gongbaek.leanfit.global.exception.BusinessException
import com.gongbaek.leanfit.global.exception.ErrorCode
import com.gongbaek.leanfit.infrastructure.database.table.UserSettingsTable
import com.gongbaek.leanfit.infrastructure.database.table.UsersTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class UserRepository {
    fun findById(id: UUID): User? =
        transaction {
            UsersTable
                .selectAll()
                .where { (UsersTable.id eq id) and (UsersTable.deletedAt.isNull()) }
                .singleOrNull()
                ?.toUser()
        }

    fun findByDeviceId(deviceId: String): User? =
        transaction {
            UsersTable
                .selectAll()
                .where { (UsersTable.deviceId eq deviceId) and (UsersTable.deletedAt.isNull()) }
                .singleOrNull()
                ?.toUser()
        }

    fun findByProviderAndProviderId(
        provider: AuthProvider,
        providerId: String,
    ): User? =
        transaction {
            UsersTable
                .selectAll()
                .where {
                    (UsersTable.provider eq provider.name) and
                        (UsersTable.providerId eq providerId) and
                        (UsersTable.deletedAt.isNull())
                }.singleOrNull()
                ?.toUser()
        }

    fun findByEmail(email: String): User? =
        transaction {
            UsersTable
                .selectAll()
                .where { (UsersTable.email eq email) and (UsersTable.deletedAt.isNull()) }
                .singleOrNull()
                ?.toUser()
        }

    fun createGuestUser(deviceId: String): User =
        transaction {
            val now = Clock.System.now()
            val initialVersion = 0L
            val id =
                UsersTable.insert {
                    it[UsersTable.deviceId] = deviceId
                    it[provider] = AuthProvider.GUEST.name
                    it[isGuest] = true
                    it[version] = initialVersion
                    it[createdAt] = now
                    it[updatedAt] = now
                } get UsersTable.id

            User(
                id = id.value,
                deviceId = deviceId,
                provider = AuthProvider.GUEST,
                providerId = null,
                email = null,
                nickname = null,
                isGuest = true,
                version = initialVersion,
                createdAt = now,
                updatedAt = now,
            )
        }

    fun createSocialUser(
        provider: AuthProvider,
        providerId: String,
        email: String?,
        nickname: String?,
    ): User =
        transaction {
            val now = Clock.System.now()
            val initialVersion = 0L
            val id =
                UsersTable.insert {
                    it[UsersTable.provider] = provider.name
                    it[UsersTable.providerId] = providerId
                    it[UsersTable.email] = email
                    it[UsersTable.nickname] = nickname
                    it[isGuest] = false
                    it[version] = initialVersion
                    it[createdAt] = now
                    it[updatedAt] = now
                } get UsersTable.id

            User(
                id = id.value,
                deviceId = null,
                provider = provider,
                providerId = providerId,
                email = email,
                nickname = nickname,
                isGuest = false,
                version = initialVersion,
                createdAt = now,
                updatedAt = now,
            )
        }

    /**
     * 닉네임 업데이트 (낙관적 잠금 적용)
     * @throws BusinessException CONCURRENT_MODIFICATION 동시성 충돌 시
     */
    fun updateNickname(
        userId: UUID,
        nickname: String,
        expectedVersion: Long,
    ): User =
        transaction {
            val now = Clock.System.now()
            val updatedCount =
                UsersTable.update({
                    (UsersTable.id eq userId) and
                        (UsersTable.version eq expectedVersion) and
                        (UsersTable.deletedAt.isNull())
                }) {
                    it[UsersTable.nickname] = nickname
                    it[version] = expectedVersion + 1
                    it[updatedAt] = now
                }

            if (updatedCount == 0) {
                throw BusinessException(ErrorCode.CONCURRENT_MODIFICATION)
            }

            findById(userId) ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        }

    fun softDelete(userId: UUID) =
        transaction {
            val now = Clock.System.now()
            UsersTable.update({ UsersTable.id eq userId }) {
                it[deletedAt] = now
            }
        }

    fun deleteGuestUser(deviceId: String) =
        transaction {
            UsersTable.deleteWhere { UsersTable.deviceId eq deviceId }
        }

    private fun ResultRow.toUser(): User =
        User(
            id = this[UsersTable.id].value,
            deviceId = this[UsersTable.deviceId],
            provider = AuthProvider.valueOf(this[UsersTable.provider]),
            providerId = this[UsersTable.providerId],
            email = this[UsersTable.email],
            nickname = this[UsersTable.nickname],
            isGuest = this[UsersTable.isGuest],
            version = this[UsersTable.version],
            createdAt = this[UsersTable.createdAt],
            updatedAt = this[UsersTable.updatedAt],
            deletedAt = this[UsersTable.deletedAt],
        )
}

class UserSettingsRepository {
    fun findByUserId(userId: UUID): UserSettings? =
        transaction {
            UserSettingsTable
                .selectAll()
                .where { UserSettingsTable.userId eq userId }
                .singleOrNull()
                ?.toUserSettings()
        }

    fun createDefault(userId: UUID): UserSettings =
        transaction {
            val now = Clock.System.now()
            UserSettingsTable.insert {
                it[UserSettingsTable.userId] = userId
                it[updatedAt] = now
            }

            UserSettings(
                userId = userId,
                notificationEnabled = true,
                notificationTime = "09:00",
                dDayAlerts = listOf(3, 1),
                updatedAt = now,
            )
        }

    fun update(
        userId: UUID,
        notificationEnabled: Boolean?,
        notificationTime: String?,
        dDayAlerts: List<Int>?,
    ): UserSettings? =
        transaction {
            val now = Clock.System.now()
            UserSettingsTable.update({ UserSettingsTable.userId eq userId }) {
                notificationEnabled?.let { value -> it[UserSettingsTable.notificationEnabled] = value }
                notificationTime?.let { value -> it[UserSettingsTable.notificationTime] = value }
                dDayAlerts?.let { value -> it[UserSettingsTable.dDayAlerts] = value.joinToString(",") }
                it[updatedAt] = now
            }
            findByUserId(userId)
        }

    fun deleteByUserId(userId: UUID) =
        transaction {
            UserSettingsTable.deleteWhere { UserSettingsTable.userId eq userId }
        }

    private fun ResultRow.toUserSettings(): UserSettings =
        UserSettings(
            userId = this[UserSettingsTable.userId].value,
            notificationEnabled = this[UserSettingsTable.notificationEnabled],
            notificationTime = this[UserSettingsTable.notificationTime],
            dDayAlerts = this[UserSettingsTable.dDayAlerts].split(",").mapNotNull { it.toIntOrNull() },
            updatedAt = this[UserSettingsTable.updatedAt],
        )
}
