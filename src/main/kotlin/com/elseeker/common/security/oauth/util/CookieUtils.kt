package com.elseeker.common.security.oauth.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie

object CookieUtils {

    // 보안 강화를 위해 SameSite 기본값은 Lax로 고정합니다.
    private const val DEFAULT_SAME_SITE = "Lax"

    fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        return request.cookies?.firstOrNull { it.name == name }
    }

    fun addCookie(
        response: HttpServletResponse,
        name: String,
        value: String,
        maxAgeSeconds: Long,
        secure: Boolean,
    ) {
        val cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(maxAgeSeconds)
            .sameSite(DEFAULT_SAME_SITE)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    fun deleteCookie(
        response: HttpServletResponse,
        name: String,
        secure: Boolean,
    ) {
        val cookie = ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(0)
            .sameSite(DEFAULT_SAME_SITE)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }
}
