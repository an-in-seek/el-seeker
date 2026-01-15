package com.elseeker.common.security

import com.elseeker.common.config.ElSeekerProperties
import com.elseeker.common.security.jwt.JwtAuthenticationFilter
import com.elseeker.common.security.oauth.handler.OAuth2LoginSuccessHandler
import com.elseeker.common.security.oauth.service.CustomOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
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
                    "/",
                    "/oauth2/**",
                    "/login/**",
                    "/error",
                    "/api/v1/**"
                ).permitAll()
                    // 정적 리소스
                    .requestMatchers(
                        "/favicon.ico",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/web/**",
                    ).permitAll()
                    .anyRequest().authenticated()
            }

            // 6. OAuth2 로그인 설정
            .oauth2Login { oauth2 ->
                oauth2.userInfoEndpoint { userInfo ->
                    userInfo.userService(customOAuth2UserService)
                }
                oauth2.successHandler(oAuth2LoginSuccessHandler)
                // 실패 핸들러도 필요하다면 추가 (.failureHandler)
            }

            // 7. JWT 필터 추가 (UsernamePasswordAuthenticationFilter 앞단에 배치)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

            // 8. 예외 처리 (인증 실패 시 로그인 폼 리다이렉트 대신 401 응답)
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }

        return http.build()
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