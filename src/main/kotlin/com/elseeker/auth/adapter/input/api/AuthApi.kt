package com.elseeker.auth.adapter.input.api

import com.elseeker.auth.adapter.input.api.response.AuthMeResponse
import com.elseeker.common.config.ElSeekerProperties
import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.common.security.jwt.JwtProvider
import com.elseeker.common.security.oauth.util.CookieUtils
import com.elseeker.member.application.service.MemberService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthApi(
    private val memberService: MemberService,
    private val jwtProvider: JwtProvider,
    private val properties: ElSeekerProperties,
) : AuthApiDocument {

    @GetMapping("/me")
    override fun me(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): AuthMeResponse {
        val member = memberService.getMemberWithOAuthAccounts(principal.memberUid)
        return AuthMeResponse.from(member)
    }

    @PostMapping("/refresh")
    override fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        val refreshToken = jwtProvider.resolveRefreshToken(request)
        val claims = refreshToken?.let(jwtProvider::resolveClaims)
        if (claims == null) {
            CookieUtils.deleteCookie(response, JwtProvider.ACCESS_TOKEN_COOKIE_NAME, request.isSecure)
            CookieUtils.deleteCookie(response, JwtProvider.REFRESH_TOKEN_COOKIE_NAME, request.isSecure)
            return ResponseEntity.status(401).build()
        }

        val memberUid = runCatching { java.util.UUID.fromString(claims.subject) }.getOrNull()
            ?: run {
                CookieUtils.deleteCookie(response, JwtProvider.ACCESS_TOKEN_COOKIE_NAME, request.isSecure)
                CookieUtils.deleteCookie(response, JwtProvider.REFRESH_TOKEN_COOKIE_NAME, request.isSecure)
                return ResponseEntity.status(401).build()
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
        return ResponseEntity.noContent().build()
    }
}
