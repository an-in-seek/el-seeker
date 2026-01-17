package com.elseeker.common.security.jwt

data class JwtPrincipal(
    val memberUid: String,
    val email: String,
    val role: String
)
