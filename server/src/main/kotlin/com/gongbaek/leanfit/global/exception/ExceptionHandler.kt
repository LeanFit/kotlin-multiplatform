package com.gongbaek.leanfit.global.exception

import com.gongbaek.leanfit.global.common.response.ErrorDetail
import com.gongbaek.leanfit.global.common.response.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<BusinessException> { call, cause ->
            call.respond(
                cause.errorCode.status,
                ErrorResponse(
                    error =
                        ErrorDetail(
                            code = cause.errorCode.code,
                            message = cause.errorCode.message,
                        ),
                ),
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error =
                        ErrorDetail(
                            code = ErrorCode.BAD_REQUEST.code,
                            message = cause.message ?: ErrorCode.BAD_REQUEST.message,
                        ),
                ),
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error =
                        ErrorDetail(
                            code = ErrorCode.INTERNAL_SERVER_ERROR.code,
                            message =
                                if (System.getenv("KTOR_ENV") == "production") {
                                    ErrorCode.INTERNAL_SERVER_ERROR.message
                                } else {
                                    cause.message ?: ErrorCode.INTERNAL_SERVER_ERROR.message
                                },
                        ),
                ),
            )
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error =
                        ErrorDetail(
                            code = "NOT_FOUND",
                            message = "Resource not found",
                        ),
                ),
            )
        }
    }
}
