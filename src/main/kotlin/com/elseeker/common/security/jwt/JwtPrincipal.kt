package com.elseeker.common.security.jwt

data class JwtPrincipal(
    val userId: Long,
    val email: String,
    val role: String
)
