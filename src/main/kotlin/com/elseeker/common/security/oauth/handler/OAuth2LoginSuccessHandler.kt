package com.elseeker.common.security.oauth.handler

import com.elseeker.common.config.ElSeekerProperties
import com.elseeker.common.security.jwt.JwtProvider
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration

/**
 * OAuth2 로그인 성공 핸들러.
 *
 * OAuth2 인증 성공 시 다음 작업을 수행합니다:
 * 1. JWT Access Token 및 Refresh Token 생성.
 * 2. 웹 클라이언트를 위해 HttpOnly 쿠키로 토큰 설정.
 * 3. 모바일 클라이언트를 위해 토큰을 쿼리 파라미터에 포함하여 루트 경로로 리다이렉트.
 */
@Component
class OAuth2LoginSuccessHandler(
    private val jwtProvider: JwtProvider,
    private val properties: ElSeekerProperties
) : SimpleUrlAuthenticationSuccessHandler() {

    /**
     * 사용자가 성공적으로 인증되었을 때 호출됩니다.
     *
     * @param request 인증 성공을 유발한 요청
     * @param response 응답 객체
     * @param authentication 인증 과정에서 생성된 인증 객체
     */
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as OAuth2User

        // CustomOAuth2UserService에서 설정한 속성들을 가져옵니다.
        val email = oAuth2User.attributes["email"] as? String
            ?: throw IllegalStateException("OAuth2 속성에서 이메일을 찾을 수 없습니다.")
        val userId = oAuth2User.attributes["userId"] as? Long
            ?: throw IllegalStateException("OAuth2 속성에서 사용자 ID를 찾을 수 없습니다.")
        val role = oAuth2User.attributes["role"] as? String
            ?: throw IllegalStateException("OAuth2 속성에서 사용자 권한을 찾을 수 없습니다.")

        // 1. JWT 토큰 생성 (Access Token, Refresh Token)
        val accessToken = jwtProvider.generateAccessToken(userId, email, role)
        val refreshToken = jwtProvider.generateRefreshToken(userId)

        // 2. 웹 클라이언트를 위한 쿠키 설정 (HttpOnly)
        addCookie(response, JwtProvider.Companion.ACCESS_TOKEN_COOKIE_NAME, accessToken, properties.jwt.accessTokenTtl)
        addCookie(response, JwtProvider.Companion.REFRESH_TOKEN_COOKIE_NAME, refreshToken, properties.jwt.refreshTokenTtl)

        // 3. 모바일 클라이언트를 위한 리다이렉트 설정 (쿼리 파라미터로 토큰 전달)
        val targetUrl = UriComponentsBuilder.fromUriString("/")
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build().toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    /**
     * HttpOnly 쿠키를 추가하는 헬퍼 메서드입니다.
     *
     * @param response HttpServletResponse 객체
     * @param name 쿠키 이름
     * @param value 쿠키 값
     * @param maxAge 쿠키 유효 기간
     */
    private fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Duration) {
        val cookie = Cookie(name, value)
        cookie.isHttpOnly = true
        cookie.path = "/"
        cookie.maxAge = maxAge.seconds.toInt()
        // cookie.secure = true // 프로덕션 환경에서는 활성화 필요
        response.addCookie(cookie)
    }
}