package com.gongbaek.leanfit.infrastructure.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UsersTable : UUIDTable("users") {
    val firebaseUid = varchar("firebase_uid", 128).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val displayName = varchar("display_name", 100).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
