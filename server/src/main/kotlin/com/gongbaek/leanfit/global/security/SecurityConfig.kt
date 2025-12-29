package com.gongbaek.leanfit.global.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gongbaek.leanfit.global.common.response.ErrorDetail
import com.gongbaek.leanfit.global.common.response.ErrorResponse
import com.gongbaek.leanfit.global.config.AppConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = AppConfig.Jwt.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(AppConfig.Jwt.secret))
                    .withAudience(AppConfig.Jwt.audience)
                    .withIssuer(AppConfig.Jwt.issuer)
                    .build(),
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        error =
                            ErrorDetail(
                                code = "AUTH_001",
                                message = "Token is not valid or has expired",
                            ),
                    ),
                )
            }
        }
    }
}
