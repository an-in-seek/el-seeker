package com.elseeker.common.security.jwt

import com.elseeker.common.config.ElSeekerProperties
import com.elseeker.common.security.oauth.util.CookieUtils
import com.elseeker.member.application.service.MemberService
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
class JwtRefreshFilter(
    private val jwtProvider: JwtProvider,
    private val memberService: MemberService,
    private val properties: ElSeekerProperties,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI == "/api/v1/auth/refresh"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val existingAuth = SecurityContextHolder.getContext().authentication
        if (existingAuth != null && existingAuth.isAuthenticated) {
            filterChain.doFilter(request, response)
            return
        }

        val accessToken = jwtProvider.resolveAccessToken(request)
        val accessClaims = accessToken?.let(jwtProvider::resolveClaims)
        if (accessClaims != null) {
            filterChain.doFilter(request, response)
            return
        }

        val refreshToken = jwtProvider.resolveRefreshToken(request)
        val refreshClaims = refreshToken?.let(jwtProvider::resolveClaims)
        if (refreshClaims == null) {
            filterChain.doFilter(request, response)
            return
        }

        val memberUid = runCatching { java.util.UUID.fromString(refreshClaims.subject) }.getOrNull()
        if (memberUid == null) {
            filterChain.doFilter(request, response)
            return
        }

        val member = memberService.getMember(memberUid)
        val roles = listOf(member.memberRole)
        val newAccessToken = jwtProvider.generateAccessToken(member.uid.toString(), member.email, roles)
        CookieUtils.addCookie(
            response,
            JwtProvider.ACCESS_TOKEN_COOKIE_NAME,
            newAccessToken,
            properties.jwt.accessTokenTtl.seconds,
            request.isSecure
        )

        val principal = JwtPrincipal(member.uid, member.email, roles)
        val authorities = roles.map { SimpleGrantedAuthority(it.key) }
        val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }
}
