package com.gongbaek.leanfit.domain.notification.controller

import com.gongbaek.leanfit.domain.notification.dto.request.RegisterFcmTokenRequest
import com.gongbaek.leanfit.domain.notification.service.NotificationService
import com.gongbaek.leanfit.global.exception.BusinessException
import com.gongbaek.leanfit.global.exception.ErrorCode
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

private val notificationService = NotificationService()

/**
 * 알림 라우트 (인증 필요)
 */
fun Route.notificationRoutes() {
    authenticate("auth-jwt") {
        route("/notifications") {
            // FCM 토큰 등록
            post("/fcm-token") {
                val userId = call.getUserId()
                val request = call.receive<RegisterFcmTokenRequest>()
                notificationService.registerFcmToken(userId, request)
                call.respond(HttpStatusCode.NoContent)
            }

            // 알림 목록 조회
            get {
                val userId = call.getUserId()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                val cursor = call.request.queryParameters["cursor"]
                val response = notificationService.getNotifications(userId, limit, cursor)
                call.respond(HttpStatusCode.OK, response)
            }

            // 특정 알림 읽음 처리
            patch("/{id}/read") {
                val userId = call.getUserId()
                val notificationId = call.getNotificationId()
                notificationService.markAsRead(notificationId, userId)
                call.respond(HttpStatusCode.NoContent)
            }

            // 모든 알림 읽음 처리
            post("/read-all") {
                val userId = call.getUserId()
                notificationService.markAllAsRead(userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun io.ktor.server.routing.RoutingCall.getUserId(): UUID {
    val principal = principal<JWTPrincipal>()
    val userIdString =
        principal?.getClaim("userId", String::class)
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED)
    return UUID.fromString(userIdString)
}

private fun io.ktor.server.routing.RoutingCall.getNotificationId(): UUID {
    val idString =
        parameters["id"]
            ?: throw BusinessException(ErrorCode.BAD_REQUEST)
    return try {
        UUID.fromString(idString)
    } catch (e: IllegalArgumentException) {
        throw BusinessException(ErrorCode.BAD_REQUEST)
    }
}
