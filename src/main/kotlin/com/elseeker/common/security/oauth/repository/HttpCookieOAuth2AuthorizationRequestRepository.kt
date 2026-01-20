package com.elseeker.common.security.oauth.repository

import com.elseeker.common.security.oauth.util.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import org.springframework.util.SerializationUtils
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class HttpCookieOAuth2AuthorizationRequestRepository :
    AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val cookie = CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME) ?: return null
        return deserialize(cookie.value)
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response)
            return
        }

        val linkFlag = request.getParameter(LINK_FLAG_PARAMETER)
        val enrichedRequest = if (!linkFlag.isNullOrBlank() && linkFlag.equals("true", ignoreCase = true)) {
            OAuth2AuthorizationRequest.from(authorizationRequest)
                .attributes { attrs ->
                    val updated = HashMap(attrs)
                    updated[LINK_FLAG_ATTRIBUTE] = true
                    updated
                }
                .build()
        } else {
            authorizationRequest
        }

        val serialized = serialize(enrichedRequest)
        CookieUtils.addCookie(
            response,
            OAUTH2_AUTH_REQUEST_COOKIE_NAME,
            serialized,
            COOKIE_EXPIRE_SECONDS,
            request.isSecure,
        )

        val returnUrl = request.getParameter(RETURN_URL_PARAMETER)
        if (!returnUrl.isNullOrBlank()) {
            val encodedReturnUrl = Base64.getUrlEncoder()
                .encodeToString(returnUrl.toByteArray(StandardCharsets.UTF_8))
            CookieUtils.addCookie(
                response,
                REDIRECT_URI_PARAM_COOKIE_NAME,
                encodedReturnUrl,
                COOKIE_EXPIRE_SECONDS,
                request.isSecure,
            )
        }
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): OAuth2AuthorizationRequest? {
        val authorizationRequest = loadAuthorizationRequest(request)
        removeAuthorizationRequestCookies(request, response)
        return authorizationRequest
    }

    fun getRedirectUriFromCookie(request: HttpServletRequest): String? {
        val cookie = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME) ?: return null
        return try {
            val decoded = Base64.getUrlDecoder().decode(cookie.value)
            String(decoded, StandardCharsets.UTF_8)
        } catch (ex: IllegalArgumentException) {
            null
        }
    }

    fun removeAuthorizationRequestCookies(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        CookieUtils.deleteCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, request.isSecure)
        CookieUtils.deleteCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, request.isSecure)
        CookieUtils.deleteCookie(response, LINK_FLAG_COOKIE_NAME, request.isSecure)
    }

    private fun serialize(authorizationRequest: OAuth2AuthorizationRequest): String {
        val bytes = SerializationUtils.serialize(authorizationRequest)
            ?: throw IllegalStateException("OAuth2AuthorizationRequest serialization failed.")
        return Base64.getUrlEncoder().encodeToString(bytes)
    }

    private fun deserialize(serialized: String): OAuth2AuthorizationRequest? {
        return try {
            val bytes = Base64.getUrlDecoder().decode(serialized)
            SerializationUtils.deserialize(bytes) as? OAuth2AuthorizationRequest
        } catch (ex: IllegalArgumentException) {
            null
        }
    }

    companion object {
        private const val OAUTH2_AUTH_REQUEST_COOKIE_NAME = "OAUTH2_AUTH_REQUEST"
        private const val REDIRECT_URI_PARAM_COOKIE_NAME = "RETURN_URL"
        private const val RETURN_URL_PARAMETER = "returnUrl"
        const val LINK_FLAG_COOKIE_NAME = "OAUTH2_LINK"
        const val LINK_FLAG_ATTRIBUTE = "oauth_link"
        private const val LINK_FLAG_PARAMETER = "link"
        private const val COOKIE_EXPIRE_SECONDS = 180L
    }
}
