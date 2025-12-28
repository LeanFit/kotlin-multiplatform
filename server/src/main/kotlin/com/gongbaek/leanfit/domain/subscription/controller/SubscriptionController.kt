package com.gongbaek.leanfit.domain.subscription.controller

import com.gongbaek.leanfit.domain.subscription.dto.request.CreateSubscriptionRequest
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
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID

private val subscriptionService = SubscriptionService()

fun Route.subscriptionRoutes() {
    route("/subscriptions") {
        authenticate("auth-jwt") {
            get {
                val userId = call.getUserId()
                val subscriptions = subscriptionService.getSubscriptions(userId)
                call.respond(HttpStatusCode.OK, ApiResponse(data = subscriptions))
            }

            post {
                val userId = call.getUserId()
                val request = call.receive<CreateSubscriptionRequest>()
                val subscription = subscriptionService.createSubscription(userId, request)
                call.respond(HttpStatusCode.Created, ApiResponse(data = subscription))
            }

            put("/{id}") {
                val userId = call.getUserId()
                val subscriptionId = call.getSubscriptionId()
                val request = call.receive<UpdateSubscriptionRequest>()
                val subscription = subscriptionService.updateSubscription(subscriptionId, userId, request)
                call.respond(HttpStatusCode.OK, ApiResponse(data = subscription))
            }

            delete("/{id}") {
                val userId = call.getUserId()
                val subscriptionId = call.getSubscriptionId()
                subscriptionService.deleteSubscription(subscriptionId, userId)
                call.respond(HttpStatusCode.OK, ApiResponse<Unit>(message = "Subscription deleted"))
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
