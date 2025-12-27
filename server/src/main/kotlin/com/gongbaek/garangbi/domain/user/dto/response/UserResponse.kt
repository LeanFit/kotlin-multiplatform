package com.gongbaek.garangbi.domain.user.dto.response

import com.gongbaek.garangbi.domain.user.entity.User
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val displayName: String?,
    val createdAt: String,
) {
    companion object {
        fun from(user: User): UserResponse =
            UserResponse(
                id = user.id.toString(),
                email = user.email,
                displayName = user.displayName,
                createdAt = user.createdAt.toString(),
            )
    }
}

fun User.toResponse(): UserResponse = UserResponse.from(this)
