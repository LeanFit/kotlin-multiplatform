package com.gongbaek.leanfit.domain.user.repository

import com.gongbaek.leanfit.domain.user.entity.User
import com.gongbaek.leanfit.infrastructure.database.table.UsersTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserRepository {
    fun findByFirebaseUid(firebaseUid: String): User? =
        transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.firebaseUid eq firebaseUid }
                .singleOrNull()
                ?.toUser()
        }

    fun findByEmail(email: String): User? =
        transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.email eq email }
                .singleOrNull()
                ?.toUser()
        }

    fun findById(id: UUID): User? =
        transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.id eq id }
                .singleOrNull()
                ?.toUser()
        }

    fun existsByFirebaseUid(firebaseUid: String): Boolean =
        transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.firebaseUid eq firebaseUid }
                .count() > 0
        }

    fun existsByEmail(email: String): Boolean =
        transaction {
            UsersTable
                .selectAll()
                .where { UsersTable.email eq email }
                .count() > 0
        }

    fun save(
        firebaseUid: String,
        email: String,
        displayName: String?,
    ): User =
        transaction {
            val now = Clock.System.now()
            val id =
                UsersTable.insert {
                    it[UsersTable.firebaseUid] = firebaseUid
                    it[UsersTable.email] = email
                    it[UsersTable.displayName] = displayName
                    it[createdAt] = now
                    it[updatedAt] = now
                } get UsersTable.id

            User(
                id = id.value,
                firebaseUid = firebaseUid,
                email = email,
                displayName = displayName,
                createdAt = now,
                updatedAt = now,
            )
        }

    private fun ResultRow.toUser(): User =
        User(
            id = this[UsersTable.id].value,
            firebaseUid = this[UsersTable.firebaseUid],
            email = this[UsersTable.email],
            displayName = this[UsersTable.displayName],
            createdAt = this[UsersTable.createdAt],
            updatedAt = this[UsersTable.updatedAt],
        )
}
