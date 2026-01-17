package com.elseeker.common.adapter.input.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AuthWebController {

    @GetMapping("/login")
    fun showLogin(): String {
        return "login/login"
    }
}
