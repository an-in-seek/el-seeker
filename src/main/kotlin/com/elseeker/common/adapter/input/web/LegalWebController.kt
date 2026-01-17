package com.elseeker.common.adapter.input.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LegalWebController {

    @GetMapping("/web/legal/terms")
    fun showTerms(): String {
        return "legal/terms"
    }

    @GetMapping("/web/legal/privacy")
    fun showPrivacy(): String {
        return "legal/privacy"
    }
}
