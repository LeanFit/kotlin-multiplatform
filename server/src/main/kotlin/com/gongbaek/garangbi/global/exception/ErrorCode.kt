package com.gongbaek.garangbi.global.exception

import io.ktor.http.HttpStatusCode

enum class ErrorCode(
    val status: HttpStatusCode,
    val code: String,
    val message: String,
) {
    // User
    USER_NOT_FOUND(HttpStatusCode.NotFound, "USER_001", "User not found"),
    USER_ALREADY_EXISTS(HttpStatusCode.Conflict, "USER_002", "User already exists"),
    INVALID_USER_DATA(HttpStatusCode.BadRequest, "USER_003", "Invalid user data"),

    // Subscription
    SUBSCRIPTION_NOT_FOUND(HttpStatusCode.NotFound, "SUB_001", "Subscription not found"),
    INVALID_SUBSCRIPTION_DATA(HttpStatusCode.BadRequest, "SUB_002", "Invalid subscription data"),
    SUBSCRIPTION_ACCESS_DENIED(HttpStatusCode.Forbidden, "SUB_003", "Access denied to this subscription"),

    // Auth
    INVALID_TOKEN(HttpStatusCode.Unauthorized, "AUTH_001", "Invalid or expired token"),
    UNAUTHORIZED(HttpStatusCode.Unauthorized, "AUTH_002", "Authentication required"),

    // Common
    INTERNAL_SERVER_ERROR(HttpStatusCode.InternalServerError, "SYS_001", "Internal server error"),
    BAD_REQUEST(HttpStatusCode.BadRequest, "SYS_002", "Bad request"),
}

class BusinessException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)
