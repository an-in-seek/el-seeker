package com.elseeker.common.security.oauth.handler

import com.elseeker.common.config.ElSeekerProperties
import com.elseeker.common.security.jwt.JwtProvider
import com.elseeker.common.security.oauth.repository.HttpCookieOAuth2AuthorizationRequestRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * OAuth2 로그인 성공 핸들러.
 *
 * [웹 기준 동작]
 * 1. 인증된 사용자 정보를 기반으로 JWT Access / Refresh Token을 생성합니다.
 * 2. 두 토큰을 HttpOnly + Secure 쿠키로 설정합니다.
 * 3. 토큰을 노출하지 않고 루트("/")로 리다이렉트합니다.
 *
 * ⚠️ 보안 원칙
 * - JWT를 URL(Query / Fragment)로 전달하지 않습니다.
 * - JWT는 JavaScript에서 접근할 수 없습니다.
 *
 * 모바일 클라이언트 대응은 추후 확장을 전제로 하며,
 * 현재 구현은 웹 클라이언트 전용입니다.
 */
@Component
class OAuth2LoginSuccessHandler(
    private val jwtProvider: JwtProvider,
    private val properties: ElSeekerProperties,
    private val authorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
) : SimpleUrlAuthenticationSuccessHandler() {

    /**
     * OAuth2 인증이 성공했을 때 호출됩니다.
     *
     * - CustomOAuth2UserService에서 주입한 사용자 속성을 사용합니다.
     * - 인증 성공 시점에서만 토큰을 발급합니다.
     */
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as OAuth2User

        // OAuth2User에 매핑된 사용자 식별 정보 추출
        val email = oAuth2User.attributes["email"] as? String
            ?: throw IllegalStateException("OAuth2 속성에서 이메일을 찾을 수 없습니다.")
        val memberUid = oAuth2User.attributes["memberUid"] as? String
            ?: throw IllegalStateException("OAuth2 속성에서 사용자 ID를 찾을 수 없습니다.")
        val role = oAuth2User.attributes["role"] as? String
            ?: throw IllegalStateException("OAuth2 속성에서 사용자 권한을 찾을 수 없습니다.")

        // 1. JWT 토큰 생성 (Access Token, Refresh Token)
        val accessToken = jwtProvider.generateAccessToken(memberUid, email, role)
        val refreshToken = jwtProvider.generateRefreshToken(memberUid)

        // 2. 웹 클라이언트를 위한 쿠키 설정 (HttpOnly, SameSite=Lax)
        addCookie(response, JwtProvider.Companion.ACCESS_TOKEN_COOKIE_NAME, accessToken, properties.jwt.accessTokenTtl)
        addCookie(response, JwtProvider.Companion.REFRESH_TOKEN_COOKIE_NAME, refreshToken, properties.jwt.refreshTokenTtl)

        // 3. 응답 처리 (현재 웹 전용)
        val returnUrl = authorizationRequestRepository.getRedirectUriFromCookie(request)
        val safeReturnUrl = returnUrl?.takeIf { it.startsWith("/") && !it.startsWith("//") }
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
        redirectStrategy.sendRedirect(request, response, safeReturnUrl ?: "/")

        /*
         * TODO: 모바일 클라이언트 확장 시 고려 사항
         *
         * 1. 클라이언트 구분
         * - OAuth2 인가 요청 시 client_type(web/mobile)을 AuthorizationRequest에 저장
         * - CustomAuthorizationRequestRepository를 통해 안전하게 복원
         *
         * 2. 토큰 전달 방식
         * - JWT를 URL로 전달하는 방식은 절대 금지
         * - RDB 기반 1회성 Authorization Code 발급 후
         *   앱에서 POST /api/v1/auth/exchange 호출로 토큰 교환
         *
         * 3. Redirect URI
         * - 웹: /
         * - 앱: 커스텀 스킴 또는 App/Universal Links 사용
         */
    }

    /**
     * HttpOnly 쿠키를 설정합니다.
     *
     * @param response HttpServletResponse 객체
     * @param name 쿠키 이름
     * @param value 쿠키 값
     * @param maxAge 쿠키 유효 기간
     */
    private fun addCookie(
        response: HttpServletResponse,
        name: String,
        value: String,
        maxAge: Duration,
    ) {
        val cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(maxAge.seconds)
            .sameSite("Lax") // CSRF 방어 핵심
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }
}
