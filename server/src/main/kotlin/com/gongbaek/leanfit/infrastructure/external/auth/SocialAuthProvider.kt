package com.gongbaek.leanfit.infrastructure.external.auth

import com.gongbaek.leanfit.domain.user.entity.AuthProvider

/**
 * 소셜 로그인 토큰 검증 결과
 */
data class SocialUserInfo(
    val providerId: String,
    val email: String?,
    val nickname: String?,
    val profileImageUrl: String? = null,
)

/**
 * 소셜 인증 제공자 인터페이스 (Strategy Pattern)
 * - 새로운 소셜 로그인 추가 시 이 인터페이스 구현
 */
interface SocialAuthProvider {
    val provider: AuthProvider

    /**
     * ID Token 검증 및 사용자 정보 추출
     * @param idToken 소셜 플랫폼에서 발급한 토큰
     * @return 검증된 사용자 정보
     * @throws SocialAuthException 검증 실패 시
     */
    suspend fun verifyToken(idToken: String): SocialUserInfo
}

/**
 * 소셜 인증 예외
 */
class SocialAuthException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * 소셜 인증 제공자 팩토리
 * - Provider에 따라 적절한 인증 제공자 반환
 */
class SocialAuthProviderFactory(
    private val providers: Map<AuthProvider, SocialAuthProvider> = emptyMap(),
) {
    fun getProvider(provider: AuthProvider): SocialAuthProvider =
        providers[provider]
            ?: throw SocialAuthException("Unsupported provider: $provider")

    companion object {
        /**
         * 기본 팩토리 생성 (실제 구현체 주입)
         */
        fun create(
            googleProvider: SocialAuthProvider? = null,
            kakaoProvider: SocialAuthProvider? = null,
            appleProvider: SocialAuthProvider? = null,
        ): SocialAuthProviderFactory {
            val providers = mutableMapOf<AuthProvider, SocialAuthProvider>()
            googleProvider?.let { providers[AuthProvider.GOOGLE] = it }
            kakaoProvider?.let { providers[AuthProvider.KAKAO] = it }
            appleProvider?.let { providers[AuthProvider.APPLE] = it }
            return SocialAuthProviderFactory(providers)
        }
    }
}
