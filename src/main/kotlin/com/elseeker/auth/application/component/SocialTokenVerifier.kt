package com.elseeker.auth.application.component

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.member.domain.vo.OAuthProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * 소셜 로그인 토큰 검증 컴포넌트.
 *
 * 앱에서 전달받은 소셜 토큰을 각 Provider API를 통해 검증하고
 * 사용자 정보를 추출합니다.
 *
 * - Google: ID Token → tokeninfo 엔드포인트로 검증 + audience(client ID) 확인
 * - Kakao: Access Token → User Info API 호출
 * - Naver: Access Token → User Info API 호출
 */
@Component
class SocialTokenVerifier(
    @Value("\${spring.security.oauth2.client.registration.google.client-id}")
    private val googleClientId: String,
) {

    private val logger = LoggerFactory.getLogger(SocialTokenVerifier::class.java)

    private val restClient: RestClient = RestClient.create()

    private val mapType = object : ParameterizedTypeReference<Map<String, Any>>() {}

    fun verify(provider: OAuthProvider, token: String): SocialUserInfo {
        return when (provider) {
            OAuthProvider.GOOGLE -> verifyGoogle(token)
            OAuthProvider.KAKAO -> verifyKakao(token)
            OAuthProvider.NAVER -> verifyNaver(token)
        }
    }

    /**
     * Google ID Token 검증.
     *
     * Google tokeninfo 엔드포인트를 호출하여 서명과 만료를 검증하고,
     * audience(aud)가 서버의 Google Client ID와 일치하는지 확인합니다.
     *
     * 주의: Android 앱에서 GoogleSignInOptions.requestIdToken() 호출 시
     * 반드시 서버(웹)의 Client ID를 serverClientId로 설정해야 합니다.
     */
    private fun verifyGoogle(idToken: String): SocialUserInfo {
        val response = try {
            restClient.get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token={idToken}", idToken)
                .retrieve()
                .body(mapType)
        } catch (e: Exception) {
            logger.warn("Google ID Token 검증 실패: ${e.message}")
            throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "google")
        } ?: throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "google")

        val aud = response["aud"] as? String
        if (aud != googleClientId) {
            logger.warn("Google ID Token audience 불일치: expected=$googleClientId, actual=$aud")
            throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "google")
        }

        return SocialUserInfo(
            provider = OAuthProvider.GOOGLE,
            providerUserId = response["sub"] as? String
                ?: throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "google"),
            email = response["email"] as? String ?: "",
            name = response["name"] as? String ?: "",
            imageUrl = response["picture"] as? String,
        )
    }

    /**
     * Kakao Access Token 검증.
     *
     * Kakao User Info API를 호출하여 토큰 유효성을 검증하고
     * 사용자 정보를 추출합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun verifyKakao(accessToken: String): SocialUserInfo {
        val response = try {
            restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .body(mapType)
        } catch (e: Exception) {
            logger.warn("Kakao Access Token 검증 실패: ${e.message}")
            throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "kakao")
        } ?: throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "kakao")

        val kakaoAccount = response["kakao_account"] as? Map<String, Any> ?: emptyMap()
        val profile = kakaoAccount["profile"] as? Map<String, Any> ?: emptyMap()

        return SocialUserInfo(
            provider = OAuthProvider.KAKAO,
            providerUserId = response["id"]?.toString()
                ?: throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "kakao"),
            email = kakaoAccount["email"] as? String ?: "",
            name = profile["nickname"] as? String ?: "",
            imageUrl = profile["profile_image_url"] as? String,
        )
    }

    /**
     * Naver Access Token 검증.
     *
     * Naver User Info API를 호출하여 토큰 유효성을 검증하고
     * 사용자 정보를 추출합니다.
     */
    @Suppress("UNCHECKED_CAST")
    private fun verifyNaver(accessToken: String): SocialUserInfo {
        val response = try {
            restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .body(mapType)
        } catch (e: Exception) {
            logger.warn("Naver Access Token 검증 실패: ${e.message}")
            throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "naver")
        } ?: throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "naver")

        val naverResponse = response["response"] as? Map<String, Any>
            ?: throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "naver")

        return SocialUserInfo(
            provider = OAuthProvider.NAVER,
            providerUserId = naverResponse["id"] as? String
                ?: throwError(ErrorType.SOCIAL_LOGIN_INVALID_TOKEN, "naver"),
            email = naverResponse["email"] as? String ?: "",
            name = naverResponse["name"] as? String
                ?: naverResponse["nickname"] as? String ?: "",
            imageUrl = naverResponse["profile_image"] as? String,
        )
    }
}

data class SocialUserInfo(
    val provider: OAuthProvider,
    val providerUserId: String,
    val email: String,
    val name: String,
    val imageUrl: String?,
)
