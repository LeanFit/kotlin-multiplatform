package com.gongbaek.leanfit.domain.dashboard.controller

import com.gongbaek.leanfit.domain.dashboard.service.DashboardService
import com.gongbaek.leanfit.global.exception.BusinessException
import com.gongbaek.leanfit.global.exception.ErrorCode
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

private val dashboardService = DashboardService()

/**
 * 대시보드 라우트 (인증 필요)
 */
fun Route.dashboardRoutes() {
    authenticate("auth-jwt") {
        route("/dashboard") {
            // 월간 요약
            get("/summary") {
                val userId = call.getUserId()
                val year = call.request.queryParameters["year"]?.toIntOrNull()
                val month = call.request.queryParameters["month"]?.toIntOrNull()
                val response = dashboardService.getSummary(userId, year, month)
                call.respond(HttpStatusCode.OK, response)
            }

            // 캘린더 뷰
            get("/calendar") {
                val userId = call.getUserId()
                val year = call.request.queryParameters["year"]?.toIntOrNull()
                val month = call.request.queryParameters["month"]?.toIntOrNull()
                val response = dashboardService.getCalendar(userId, year, month)
                call.respond(HttpStatusCode.OK, response)
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
