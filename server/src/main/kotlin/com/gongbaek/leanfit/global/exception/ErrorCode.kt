package com.gongbaek.leanfit.global.exception

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
    INVALID_NICKNAME(HttpStatusCode.BadRequest, "USER_004", "Invalid nickname format"),
    FORBIDDEN_NICKNAME(HttpStatusCode.BadRequest, "USER_005", "Nickname contains forbidden words"),
    INVALID_CONFIRM_TEXT(HttpStatusCode.BadRequest, "USER_006", "Confirm text does not match"),

    // Subscription
    SUBSCRIPTION_NOT_FOUND(HttpStatusCode.NotFound, "SUB_001", "Subscription not found"),
    INVALID_SUBSCRIPTION_DATA(HttpStatusCode.BadRequest, "SUB_002", "Invalid subscription data"),
    SUBSCRIPTION_ACCESS_DENIED(HttpStatusCode.Forbidden, "SUB_003", "Access denied to this subscription"),
    DUPLICATE_SUBSCRIPTION(HttpStatusCode.Conflict, "SUB_004", "Duplicate subscription exists"),
    INVALID_PAYMENT_DAY(HttpStatusCode.BadRequest, "SUB_005", "Payment day must be between 1 and 31"),
    INVALID_AMOUNT(HttpStatusCode.BadRequest, "SUB_006", "Amount must be 0 or positive"),

    // Auth
    INVALID_TOKEN(HttpStatusCode.Unauthorized, "AUTH_001", "Invalid or expired token"),
    UNAUTHORIZED(HttpStatusCode.Unauthorized, "AUTH_002", "Authentication required"),
    INVALID_DEVICE_ID(HttpStatusCode.BadRequest, "AUTH_003", "Invalid device ID format"),
    INVALID_PROVIDER(HttpStatusCode.BadRequest, "AUTH_004", "Unsupported provider"),
    EXPIRED_REFRESH_TOKEN(HttpStatusCode.Unauthorized, "AUTH_005", "Refresh token has expired"),
    ACCOUNT_EXISTS(HttpStatusCode.Conflict, "AUTH_006", "Account already exists with different provider"),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatusCode.NotFound, "NOTI_001", "Notification not found"),
    INVALID_FCM_TOKEN(HttpStatusCode.BadRequest, "NOTI_002", "Invalid FCM token"),

    // Concurrency & Idempotency
    CONCURRENT_MODIFICATION(HttpStatusCode.Conflict, "SYS_003", "Resource was modified by another request"),
    DUPLICATE_REQUEST(HttpStatusCode.Conflict, "SYS_004", "Duplicate request detected"),

    // Common
    INTERNAL_SERVER_ERROR(HttpStatusCode.InternalServerError, "SYS_001", "Internal server error"),
    BAD_REQUEST(HttpStatusCode.BadRequest, "SYS_002", "Bad request"),
}

class BusinessException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)
