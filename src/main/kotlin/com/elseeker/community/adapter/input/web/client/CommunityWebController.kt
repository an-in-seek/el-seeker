package com.elseeker.community.adapter.input.web.client

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/web/community")
class CommunityWebController {

    @GetMapping
    fun showCommunityHome(): String {
        return "community/community"
    }

    @GetMapping("/write")
    fun showCommunityWrite(authentication: Authentication?): String {
        redirectIfUnauthenticated(authentication, "/web/community/write")?.let { return it }
        return "community/community-write"
    }

    @GetMapping("/{postId}")
    fun showCommunityDetail(
        @PathVariable postId: Long,
        model: Model,
    ): String {
        model.addAttribute("postId", postId)
        return "community/community-detail"
    }

    private fun redirectIfUnauthenticated(authentication: Authentication?, returnUrl: String): String? {
        if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            return "redirect:/web/auth/login?returnUrl=$returnUrl"
        }
        return null
    }
}