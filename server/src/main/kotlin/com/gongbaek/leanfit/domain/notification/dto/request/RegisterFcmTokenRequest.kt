package com.gongbaek.leanfit.domain.notification.dto.request

import kotlinx.serialization.Serializable

/**
 * FCM 토큰 등록 요청
 * POST /notifications/fcm-token
 */
@Serializable
data class RegisterFcmTokenRequest(
    val fcmToken: String,
    val platform: String,
) {
    init {
        require(fcmToken.isNotBlank()) { "fcmToken must not be blank" }
        require(platform in listOf("ANDROID", "IOS")) { "platform must be ANDROID or IOS" }
    }
}
