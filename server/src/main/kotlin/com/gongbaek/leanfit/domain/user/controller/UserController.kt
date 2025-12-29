package com.gongbaek.leanfit.domain.user.controller

import com.gongbaek.leanfit.domain.user.dto.request.DeleteUserRequest
import com.gongbaek.leanfit.domain.user.dto.request.GuestAuthRequest
import com.gongbaek.leanfit.domain.user.dto.request.LogoutRequest
import com.gongbaek.leanfit.domain.user.dto.request.RefreshTokenRequest
import com.gongbaek.leanfit.domain.user.dto.request.SocialAuthRequest
import com.gongbaek.leanfit.domain.user.dto.request.UpdateUserRequest
import com.gongbaek.leanfit.domain.user.dto.request.UpdateUserSettingsRequest
import com.gongbaek.leanfit.domain.user.service.UserService
import com.gongbaek.leanfit.global.common.response.ApiResponse
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
import io.ktor.server.routing.route
import java.util.UUID

private val userService = UserService()

/**
 * 인증 라우트 (공개)
 */
fun Route.authRoutes() {
    route("/auth") {
        // 게스트 인증
        post("/guest") {
            val request = call.receive<GuestAuthRequest>()
            val response = userService.authenticateGuest(request)
            call.respond(HttpStatusCode.OK, response)
        }

        // 소셜 로그인
        post("/social") {
            val request = call.receive<SocialAuthRequest>()
            val response = userService.authenticateSocial(request)
            call.respond(HttpStatusCode.OK, response)
        }

        // 토큰 갱신
        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val response = userService.refreshToken(request)
            call.respond(HttpStatusCode.OK, response)
        }

        // 로그아웃
        authenticate("auth-jwt") {
            post("/logout") {
                val userId = call.getUserId()
                val request = call.receive<LogoutRequest>()
                userService.logout(userId, request.refreshToken)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

/**
 * 유저 라우트 (인증 필요)
 */
fun Route.userRoutes() {
    authenticate("auth-jwt") {
        route("/users") {
            // 내 정보 조회
            get("/me") {
                val userId = call.getUserId()
                val response = userService.getMyInfo(userId)
                call.respond(HttpStatusCode.OK, ApiResponse(data = response))
            }

            // 닉네임 수정
            patch("/me") {
                val userId = call.getUserId()
                val request = call.receive<UpdateUserRequest>()
                val response = userService.updateNickname(userId, request)
                call.respond(HttpStatusCode.OK, ApiResponse(data = response))
            }

            // 설정 변경
            patch("/me/settings") {
                val userId = call.getUserId()
                val request = call.receive<UpdateUserSettingsRequest>()
                val response = userService.updateSettings(userId, request)
                call.respond(HttpStatusCode.OK, ApiResponse(data = response))
            }

            // 탈퇴
            delete("/me") {
                val userId = call.getUserId()
                val request = call.receive<DeleteUserRequest>()
                userService.deleteUser(userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

/**
 * JWT에서 userId 추출
 */
private fun io.ktor.server.application.ApplicationCall.getUserId(): UUID {
    val principal = principal<JWTPrincipal>()
    val userId = principal?.payload?.getClaim("userId")?.asString()
    return UUID.fromString(userId)
}
