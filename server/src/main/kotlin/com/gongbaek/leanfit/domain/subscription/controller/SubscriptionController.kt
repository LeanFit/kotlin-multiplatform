package com.gongbaek.leanfit.domain.subscription.controller

import com.gongbaek.leanfit.domain.subscription.dto.request.CreateSubscriptionRequest
import com.gongbaek.leanfit.domain.subscription.dto.request.UpdateStatusRequest
import com.gongbaek.leanfit.domain.subscription.dto.request.UpdateSubscriptionRequest
import com.gongbaek.leanfit.domain.subscription.service.SubscriptionService
import com.gongbaek.leanfit.global.common.response.ApiResponse
import com.gongbaek.leanfit.global.exception.BusinessException
import com.gongbaek.leanfit.global.exception.ErrorCode
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID

private val subscriptionService = SubscriptionService()

/**
 * 구독 라우트 (인증 필요)
 */
fun Route.subscriptionRoutes() {
    authenticate("auth-jwt") {
        route("/subscriptions") {
            // 구독 목록 조회
            get {
                val userId = call.getUserId()
                val status = call.request.queryParameters["status"]
                val response = subscriptionService.getSubscriptions(userId, status)
                call.respond(HttpStatusCode.OK, response)
            }

            // 구독 상세 조회
            get("/{id}") {
                val userId = call.getUserId()
                val subscriptionId = call.getSubscriptionId()
                val response = subscriptionService.getSubscription(subscriptionId, userId)
                call.respond(HttpStatusCode.OK, ApiResponse(data = response))
            }

            // 구독 추가
            post {
                val userId = call.getUserId()
                val request = call.receive<CreateSubscriptionRequest>()
                val response = subscriptionService.createSubscription(userId, request)
                call.respond(HttpStatusCode.Created, response)
            }

            // 구독 수정
            put("/{id}") {
                val userId = call.getUserId()
                val subscriptionId = call.getSubscriptionId()
                val request = call.receive<UpdateSubscriptionRequest>()
                val response = subscriptionService.updateSubscription(subscriptionId, userId, request)
                call.respond(HttpStatusCode.OK, ApiResponse(data = response))
            }

            // 구독 삭제
            delete("/{id}") {
                val userId = call.getUserId()
                val subscriptionId = call.getSubscriptionId()
                subscriptionService.deleteSubscription(subscriptionId, userId)
                call.respond(HttpStatusCode.NoContent)
            }

            // 구독 상태 변경
            patch("/{id}/status") {
                val userId = call.getUserId()
                val subscriptionId = call.getSubscriptionId()
                val request = call.receive<UpdateStatusRequest>()
                val response = subscriptionService.updateStatus(subscriptionId, userId, request)
                call.respond(HttpStatusCode.OK, ApiResponse(data = response))
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

private fun io.ktor.server.routing.RoutingCall.getSubscriptionId(): UUID {
    val idString =
        parameters["id"]
            ?: throw BusinessException(ErrorCode.BAD_REQUEST)
    return try {
        UUID.fromString(idString)
    } catch (e: IllegalArgumentException) {
        throw BusinessException(ErrorCode.BAD_REQUEST)
    }
}
