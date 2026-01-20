package com.elseeker.common.security.oauth.handler

import com.elseeker.common.domain.ServiceError
import com.elseeker.common.security.oauth.repository.HttpCookieOAuth2AuthorizationRequestRepository
import com.elseeker.common.security.oauth.util.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class OAuth2LoginFailureHandler(
    private val authorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
) : SimpleUrlAuthenticationFailureHandler() {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val returnUrl = authorizationRequestRepository.getRedirectUriFromCookie(request)
        val safeReturnUrl = returnUrl?.takeIf { it.startsWith("/") && !it.startsWith("//") } ?: "/web/auth/login"

        val errorType = (exception.cause as? ServiceError)?.errorType?.name ?: "UNKNOWN"
        val encodedError = URLEncoder.encode(errorType, StandardCharsets.UTF_8)
        val separator = if (safeReturnUrl.contains("?")) "&" else "?"
        val redirectUrl = "$safeReturnUrl${separator}oauthError=$encodedError"

        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
        CookieUtils.deleteCookie(response, HttpCookieOAuth2AuthorizationRequestRepository.LINK_FLAG_COOKIE_NAME, request.isSecure)
        redirectStrategy.sendRedirect(request, response, redirectUrl)
    }
}
