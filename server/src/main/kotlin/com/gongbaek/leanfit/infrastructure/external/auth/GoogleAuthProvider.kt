package com.gongbaek.leanfit.infrastructure.external.auth

import com.gongbaek.leanfit.domain.user.entity.AuthProvider

/**
 * Google OAuth 인증 제공자
 * - Firebase Admin SDK를 통한 ID Token 검증
 *
 * 실제 구현 시:
 * 1. Firebase Admin SDK 의존성 추가
 * 2. Firebase 서비스 계정 키 설정
 * 3. FirebaseAuth.getInstance().verifyIdToken() 호출
 */
class GoogleAuthProvider : SocialAuthProvider {
    override val provider: AuthProvider = AuthProvider.GOOGLE

    override suspend fun verifyToken(idToken: String): SocialUserInfo {
        // TODO: Firebase Admin SDK 구현
        // val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
        // return SocialUserInfo(
        //     providerId = decodedToken.uid,
        //     email = decodedToken.email,
        //     nickname = decodedToken.name,
        //     profileImageUrl = decodedToken.picture,
        // )

        throw SocialAuthException("Google authentication not yet implemented")
    }
}

/**
 * Kakao OAuth 인증 제공자
 * - Kakao REST API를 통한 Access Token 검증
 *
 * 실제 구현 시:
 * 1. Kakao Admin Key 설정
 * 2. https://kapi.kakao.com/v2/user/me API 호출
 */
class KakaoAuthProvider : SocialAuthProvider {
    override val provider: AuthProvider = AuthProvider.KAKAO

    override suspend fun verifyToken(idToken: String): SocialUserInfo {
        // TODO: Kakao REST API 구현
        // val response = httpClient.get("https://kapi.kakao.com/v2/user/me") {
        //     headers { append("Authorization", "Bearer $idToken") }
        // }
        // return SocialUserInfo(
        //     providerId = response.id.toString(),
        //     email = response.kakaoAccount?.email,
        //     nickname = response.kakaoAccount?.profile?.nickname,
        // )

        throw SocialAuthException("Kakao authentication not yet implemented")
    }
}

/**
 * Apple OAuth 인증 제공자
 * - Firebase Admin SDK 또는 Apple JWT 직접 검증
 *
 * 실제 구현 시:
 * 1. Apple Developer 설정
 * 2. Identity Token (JWT) 검증
 */
class AppleAuthProvider : SocialAuthProvider {
    override val provider: AuthProvider = AuthProvider.APPLE

    override suspend fun verifyToken(idToken: String): SocialUserInfo {
        // TODO: Apple Sign-In 구현
        // Firebase를 통하거나 직접 JWT 검증
        // val jwt = JWT.decode(idToken)
        // return SocialUserInfo(
        //     providerId = jwt.subject,
        //     email = jwt.getClaim("email").asString(),
        //     nickname = null, // Apple은 이름을 첫 로그인 시에만 제공
        // )

        throw SocialAuthException("Apple authentication not yet implemented")
    }
}
