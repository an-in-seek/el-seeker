package com.elseeker.auth.adapter.input.web

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AuthWebController {

    @GetMapping("/web/auth/login")
    fun showLogin(
        @RequestParam(required = false) returnUrl: String?,
        authentication: Authentication?
    ): String {
        // SSR 접근 시에도 서버가 인증 여부를 확인하고 적절히 이동시킵니다.
        if (authentication != null && authentication.isAuthenticated && authentication.principal != "anonymousUser") {
            val safeReturnUrl = returnUrl?.takeIf { it.startsWith("/") && !it.startsWith("//") }
            return "redirect:${safeReturnUrl ?: "/web/game"}"
        }
        return "login/login"
    }
}
