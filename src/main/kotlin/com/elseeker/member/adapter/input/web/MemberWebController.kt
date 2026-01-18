package com.elseeker.member.adapter.input.web

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/web/member")
class MemberWebController {

    @GetMapping("/withdraw")
    fun showWithdraw(authentication: Authentication?): String {
        redirectIfUnauthenticated(authentication, "/web/member/withdraw")?.let { return it }
        return "member/withdraw"
    }

    @GetMapping("/mypage")
    fun showMyPage(authentication: Authentication?): String {
        redirectIfUnauthenticated(authentication, "/web/member/mypage")?.let { return it }
        return "member/mypage"
    }

    @GetMapping("/withdraw/complete")
    fun showWithdrawComplete(): String {
        return "member/withdraw-complete"
    }

    private fun redirectIfUnauthenticated(authentication: Authentication?, returnUrl: String): String? {
        if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            return "redirect:/web/auth/login?returnUrl=$returnUrl"
        }
        return null
    }
}
