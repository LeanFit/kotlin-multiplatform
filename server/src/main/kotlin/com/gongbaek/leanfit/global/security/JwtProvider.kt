package com.gongbaek.leanfit.global.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.gongbaek.leanfit.global.config.AppConfig
import java.security.MessageDigest
import java.util.Date
import java.util.UUID

object JwtProvider {
    private const val ACCESS_TOKEN_EXPIRATION_HOURS: Long = 1
    private const val REFRESH_TOKEN_EXPIRATION_DAYS: Long = 30

    /**
     * Access Token 생성 (1시간 유효)
     */
    fun generateAccessToken(userId: String): String =
        JWT
            .create()
            .withAudience(AppConfig.Jwt.audience)
            .withIssuer(AppConfig.Jwt.issuer)
            .withClaim("userId", userId)
            .withClaim("type", "access")
            .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_HOURS * 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(AppConfig.Jwt.secret))

    /**
     * Refresh Token 생성 (30일 유효)
     */
    fun generateRefreshToken(userId: String): String =
        JWT
            .create()
            .withAudience(AppConfig.Jwt.audience)
            .withIssuer(AppConfig.Jwt.issuer)
            .withClaim("userId", userId)
            .withClaim("type", "refresh")
            .withJWTId(UUID.randomUUID().toString())
            .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_DAYS * 24 * 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(AppConfig.Jwt.secret))

    /**
     * Access Token 만료 시간 (초)
     */
    fun getAccessTokenExpiresIn(): Long = ACCESS_TOKEN_EXPIRATION_HOURS * 60 * 60

    /**
     * Refresh Token에서 userId 추출
     */
    fun getUserIdFromRefreshToken(refreshToken: String): String? =
        try {
            val verifier =
                JWT
                    .require(Algorithm.HMAC256(AppConfig.Jwt.secret))
                    .withAudience(AppConfig.Jwt.audience)
                    .withIssuer(AppConfig.Jwt.issuer)
                    .withClaim("type", "refresh")
                    .build()

            val decodedJWT = verifier.verify(refreshToken)
            decodedJWT.getClaim("userId").asString()
        } catch (e: JWTVerificationException) {
            null
        }

    /**
     * Refresh Token 해시 생성 (DB 저장용)
     */
    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
