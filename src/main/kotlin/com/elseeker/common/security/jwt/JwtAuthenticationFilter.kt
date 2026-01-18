package com.elseeker.common.security.jwt

import com.elseeker.member.domain.vo.MemberRole
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = jwtProvider.resolveAccessToken(request)
        // 토큰이 존재하면 파싱 시도 (파싱 성공 시 claims 반환, 실패 시 null)
        if (!token.isNullOrBlank()) {
            val claims = jwtProvider.resolveClaims(token)
            if (claims != null) {
                val memberUid = runCatching { java.util.UUID.fromString(claims.subject) }.getOrNull()
                val email = claims["email"]?.toString().orEmpty()
                val rolesClaim = claims["roles"]
                val roles = when (rolesClaim) {
                    is Collection<*> -> rolesClaim.mapNotNull { it?.toString() }
                    is Array<*> -> rolesClaim.mapNotNull { it?.toString() }
                    else -> emptyList()
                }.mapNotNull { roleValue ->
                    runCatching { MemberRole.valueOf(roleValue) }.getOrNull()
                        ?: MemberRole.fromKey(roleValue)
                }

                if (memberUid != null && roles.isNotEmpty()) {
                    val principal = JwtPrincipal(memberUid, email, roles)
                    val authorities = roles.map { SimpleGrantedAuthority(it.key) }

                    val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}
