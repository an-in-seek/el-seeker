package com.elseeker.community.adapter.input.web

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

    @GetMapping("/{postId}")
    fun showCommunityDetail(
        @PathVariable postId: Long,
        model: Model,
    ): String {
        model.addAttribute("postId", postId)
        return "community/community-detail"
    }
}
