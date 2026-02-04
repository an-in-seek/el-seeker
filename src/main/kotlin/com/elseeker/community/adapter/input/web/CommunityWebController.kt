package com.elseeker.community.adapter.input.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/web/community")
class CommunityWebController {

    @GetMapping
    fun showCommunityHome(): String {
        return "community/community"
    }
}
