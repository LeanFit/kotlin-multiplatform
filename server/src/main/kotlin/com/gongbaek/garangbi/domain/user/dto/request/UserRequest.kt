package com.gongbaek.garangbi.domain.user.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val firebaseUid: String,
    val email: String,
    val displayName: String? = null,
) {
    init {
        require(firebaseUid.isNotBlank()) { "firebaseUid must not be blank" }
        require(email.isNotBlank()) { "email must not be blank" }
        require(email.contains("@")) { "email format is invalid" }
    }
}

@Serializable
data class LoginRequest(
    val firebaseUid: String,
) {
    init {
        require(firebaseUid.isNotBlank()) { "firebaseUid must not be blank" }
    }
}
