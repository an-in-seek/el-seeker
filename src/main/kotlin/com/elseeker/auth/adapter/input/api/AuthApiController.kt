package com.elseeker.auth.adapter.input.api

import com.elseeker.auth.adapter.input.api.response.AuthMeResponse
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthApiController {

    @GetMapping("/me")
    fun me(authentication: Authentication?): ResponseEntity<AuthMeResponse> {
        // 서버가 최종 인증 책임을 가지므로, SecurityContext 기준으로만 응답합니다.
        val principal = authentication?.principal
        if (authentication == null || !authentication.isAuthenticated || principal !is JwtPrincipal) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        return ResponseEntity.ok(AuthMeResponse.from(principal))
    }
}