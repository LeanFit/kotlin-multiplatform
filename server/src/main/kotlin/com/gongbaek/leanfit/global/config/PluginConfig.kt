package com.gongbaek.leanfit.global.config

import com.gongbaek.leanfit.domain.dashboard.controller.dashboardRoutes
import com.gongbaek.leanfit.domain.notification.controller.notificationRoutes
import com.gongbaek.leanfit.domain.subscription.controller.subscriptionRoutes
import com.gongbaek.leanfit.domain.user.controller.authRoutes
import com.gongbaek.leanfit.domain.user.controller.userRoutes
import com.gongbaek.leanfit.global.common.idempotency.IdempotencyPlugin
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            },
        )
    }
}

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("Idempotency-Key")
        anyHost()
    }

    // 멱등성 처리 플러그인
    install(IdempotencyPlugin) {
        excludedPaths = setOf("/auth/", "/health", "/dashboard/")
    }
}

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}

fun Application.configureRouting() {
    routing {
        healthRoutes()
        authRoutes()
        userRoutes()
        subscriptionRoutes()
        dashboardRoutes()
        notificationRoutes()
    }
}

private fun Route.healthRoutes() {
    get("/health") {
        call.respond(
            HttpStatusCode.OK,
            mapOf(
                "status" to "healthy",
                "service" to "leanfit-api",
            ),
        )
    }

    get("/") {
        call.respond(
            HttpStatusCode.OK,
            mapOf(
                "message" to "Welcome to LeanFit API",
                "version" to "1.0.0",
            ),
        )
    }
}
