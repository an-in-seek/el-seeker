package com.elseeker.common.security

import com.elseeker.common.config.ElSeekerProperties
import com.elseeker.common.security.jwt.JwtAuthenticationFilter
import com.elseeker.common.security.jwt.JwtRefreshFilter
import com.elseeker.common.security.oauth.handler.OAuth2LoginFailureHandler
import com.elseeker.common.security.oauth.handler.OAuth2LoginSuccessHandler
import com.elseeker.common.security.oauth.repository.HttpCookieOAuth2AuthorizationRequestRepository
import com.elseeker.common.security.oauth.service.CustomOAuth2UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val oAuth2LoginFailureHandler: OAuth2LoginFailureHandler,
    private val authorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtRefreshFilter: JwtRefreshFilter,
    private val elSeekerProperties: ElSeekerProperties,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 1. CSRF 비활성화 (JWT 사용 시 불필요)
            .csrf { it.disable() }

            // 2. 기본 인증 방식 비활성화 (API 서버이므로)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }

            // 3. CORS 설정 적용
            .cors { it.configurationSource(corsConfigurationSource()) }

            // 4. 세션 관리: Stateless (서버에 세션 유지 X)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // 5. 요청 권한 관리
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/memos",
                    "/api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/verses/{verseNumber}/memo"
                ).authenticated()
                    .requestMatchers(
                    "/",
                    "/error",
                    "/oauth2/**",
                    "/web/auth/login",
                    "/web/auth/login/**",
                    "/web/auth/logout",
                    "/web/auth/logout/**",
                    "/web/game",
                    "/api/v1/bibles/**",
                    "/api/v1/study/dictionaries/**",
                    "/api/v1/auth/refresh"
                ).permitAll()
                    .requestMatchers(
                    "/api/v1/auth/me",
                    "/api/v1/members/**",
                    "/api/v1/game/bible-quiz/**"
                ).authenticated()
                    // 게임 영역은 서버에서 인증을 강제합니다. (UX용 JS는 보조 역할)
                    .requestMatchers(
                        "/web/game/**",
                        "/game/**",
                    ).authenticated()
                    // 정적 리소스
                    .requestMatchers(
                        "/favicon.ico",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                    ).permitAll()
                    // 그 외 공개 SSR 페이지
                    .requestMatchers("/web/**").permitAll()
                    .anyRequest().authenticated()
            }

            // 6. OAuth2 로그인 설정
            .oauth2Login { oauth2 ->
                oauth2.authorizationEndpoint { authorizationEndpoint ->
                    authorizationEndpoint.authorizationRequestRepository(authorizationRequestRepository)
                }
                oauth2.userInfoEndpoint { userInfo ->
                    userInfo.userService(customOAuth2UserService)
                }
                oauth2.successHandler(oAuth2LoginSuccessHandler)
                oauth2.failureHandler(oAuth2LoginFailureHandler)
            }

            // 7. JWT 필터 추가 (UsernamePasswordAuthenticationFilter 앞단에 배치)
            .addFilterBefore(jwtRefreshFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

            // 8. 예외 처리 (SSR 페이지는 로그인으로, API는 401을 반환)
            .exceptionHandling {
                it.authenticationEntryPoint { request, response, _ ->
                    val acceptHeader = request.getHeader("Accept").orEmpty()
                    val isHtmlRequest = acceptHeader.contains("text/html")
                    if (isHtmlRequest && request.requestURI.startsWith("/web/")) {
                        val returnUrl = URLEncoder.encode(buildReturnUrl(request), StandardCharsets.UTF_8)
                        response.sendRedirect("/web/auth/login?returnUrl=$returnUrl")
                    } else {
                        response.sendError(HttpStatus.UNAUTHORIZED.value())
                    }
                }
            }

        return http.build()
    }

    private fun buildReturnUrl(request: HttpServletRequest): String {
        val query = request.queryString?.let { "?$it" }.orEmpty()
        return "${request.requestURI}$query"
    }

    /**
     * CORS 설정 빈
     * 프론트엔드 도메인, 허용할 메서드 및 헤더를 정의합니다.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = listOf(elSeekerProperties.api.baseUrl)
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true // 쿠키(리프레시 토큰) 전송을 위해 필요

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
