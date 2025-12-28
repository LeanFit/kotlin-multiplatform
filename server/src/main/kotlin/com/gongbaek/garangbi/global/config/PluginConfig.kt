package com.gongbaek.garangbi.global.config

import com.gongbaek.garangbi.domain.subscription.controller.subscriptionRoutes
import com.gongbaek.garangbi.domain.user.controller.authRoutes
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
        anyHost()
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
        subscriptionRoutes()
    }
}

private fun Route.healthRoutes() {
    get("/health") {
        call.respond(
            HttpStatusCode.OK,
            mapOf(
                "status" to "healthy",
                "service" to "garangbi-api",
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
