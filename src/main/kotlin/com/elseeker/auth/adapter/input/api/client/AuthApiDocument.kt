package com.elseeker.auth.adapter.input.api.client

import com.elseeker.auth.adapter.input.api.client.request.SocialLoginRequest
import com.elseeker.auth.adapter.input.api.client.response.AuthMeResponse
import com.elseeker.auth.adapter.input.api.client.response.SocialLoginResponse
import com.elseeker.common.security.jwt.JwtPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity

@Tag(name = "Auth", description = "Authentication endpoints")
interface AuthApiDocument {

    @Operation(summary = "Get current authenticated member")
    fun me(principal: JwtPrincipal): AuthMeResponse

    @Operation(summary = "Refresh access token using refresh token cookie")
    fun refresh(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Void>

    @Operation(
        summary = "모바일 소셜 로그인",
        description = "앱에서 네이티브 SDK로 획득한 소셜 토큰을 검증하고 JWT를 발급합니다. " +
            "Google: ID Token, Kakao/Naver: Access Token을 전달합니다."
    )
    fun socialLogin(request: SocialLoginRequest): SocialLoginResponse
}
