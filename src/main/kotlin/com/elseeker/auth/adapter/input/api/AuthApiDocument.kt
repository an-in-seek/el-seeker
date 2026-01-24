package com.elseeker.auth.adapter.input.api

import com.elseeker.auth.adapter.input.api.response.AuthMeResponse
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
}
