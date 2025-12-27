package com.gongbaek.garangbi.domain.user.controller

import com.gongbaek.garangbi.domain.user.dto.request.LoginRequest
import com.gongbaek.garangbi.domain.user.dto.request.RegisterRequest
import com.gongbaek.garangbi.domain.user.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

private val userService = UserService()

fun Route.authRoutes() {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = userService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = userService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
