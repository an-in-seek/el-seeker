package com.elseeker.auth.adapter.input.api.response

import com.elseeker.common.security.jwt.JwtPrincipal

data class AuthMeResponse(
    val memberUid: String,
    val email: String,
    val role: String,
) {
    companion object {
        fun from(principal: JwtPrincipal): AuthMeResponse {
            return AuthMeResponse(
                memberUid = principal.memberUid,
                email = principal.email,
                role = principal.role,
            )
        }
    }
}
