package com.gongbaek.leanfit.global.common.idempotency

import com.gongbaek.leanfit.global.common.response.ErrorDetail
import com.gongbaek.leanfit.global.common.response.ErrorResponse
import com.gongbaek.leanfit.global.exception.ErrorCode
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respondText
import io.ktor.util.AttributeKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * 멱등성 처리 플러그인
 *
 * POST, PUT, DELETE 요청에 대해 Idempotency-Key 헤더를 확인하고
 * 동일한 요청이 이미 처리된 경우 캐싱된 응답을 반환합니다.
 */
class IdempotencyPlugin(
    private val config: Configuration,
) {
    class Configuration {
        var idempotencyRepository: IdempotencyRepository = IdempotencyRepository()
        var enabledMethods: Set<HttpMethod> = setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Delete)
        var excludedPaths: Set<String> = setOf("/auth/", "/health")
    }

    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, IdempotencyPlugin> {
        override val key = AttributeKey<IdempotencyPlugin>("IdempotencyPlugin")

        const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"
        val IDEMPOTENCY_RESPONSE_KEY = AttributeKey<IdempotencyResponse>("IdempotencyResponse")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit,
        ): IdempotencyPlugin {
            val configuration = Configuration().apply(configure)
            val plugin = IdempotencyPlugin(configuration)

            pipeline.intercept(ApplicationCallPipeline.Plugins) {
                plugin.intercept(call)
            }

            return plugin
        }
    }

    private suspend fun intercept(call: ApplicationCall) {
        val method = call.request.local.method
        val path = call.request.local.uri

        // 활성화된 메서드가 아니거나 제외된 경로인 경우 스킵
        if (method !in config.enabledMethods) return
        if (config.excludedPaths.any { path.startsWith(it) }) return

        val idempotencyKey = call.request.headers[IDEMPOTENCY_KEY_HEADER]

        // 멱등성 키가 없는 경우 그냥 진행 (필수가 아님)
        if (idempotencyKey.isNullOrBlank()) return

        // 사용자 ID 추출 (인증된 요청만 멱등성 처리)
        val userId = call.getUserIdOrNull() ?: return

        // 기존 처리된 요청 확인
        val existingResponse = config.idempotencyRepository.findByKeyAndUserId(idempotencyKey, userId)

        if (existingResponse != null) {
            // 요청 경로와 메서드가 일치하는지 확인
            if (existingResponse.requestPath != path || existingResponse.requestMethod != method.value) {
                call.respondText(
                    text =
                        Json.encodeToString(
                            ErrorResponse(
                                error =
                                    ErrorDetail(
                                        code = ErrorCode.DUPLICATE_REQUEST.code,
                                        message = "Idempotency key already used for different request",
                                    ),
                            ),
                        ),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.Conflict,
                )
                return
            }

            // 캐싱된 응답 반환
            call.respondText(
                text = existingResponse.responseBody,
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.fromValue(existingResponse.responseStatus),
            )
            return
        }

        // 응답 저장을 위한 컨텍스트 설정
        call.attributes.put(
            IDEMPOTENCY_RESPONSE_KEY,
            IdempotencyResponse(
                key = idempotencyKey,
                userId = userId,
                requestPath = path,
                requestMethod = method.value,
            ),
        )
    }

    private fun ApplicationCall.getUserIdOrNull(): UUID? =
        try {
            val principal = principal<JWTPrincipal>()
            principal
                ?.payload
                ?.getClaim("userId")
                ?.asString()
                ?.let { UUID.fromString(it) }
        } catch (e: Exception) {
            null
        }
}

/**
 * 응답 저장용 데이터 클래스
 */
data class IdempotencyResponse(
    val key: String,
    val userId: UUID,
    val requestPath: String,
    val requestMethod: String,
)
