package com.gongbaek.garangbi.global.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gongbaek.garangbi.global.config.AppConfig
import java.util.Date

object JwtProvider {
    fun generateToken(userId: String): String =
        JWT
            .create()
            .withAudience(AppConfig.Jwt.audience)
            .withIssuer(AppConfig.Jwt.issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + AppConfig.Jwt.expirationDays * 24 * 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(AppConfig.Jwt.secret))
}
