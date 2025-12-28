package com.gongbaek.leanfit.domain.user.entity

import kotlinx.datetime.Instant
import java.util.UUID

data class User(
    val id: UUID,
    val firebaseUid: String,
    val email: String,
    val displayName: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
