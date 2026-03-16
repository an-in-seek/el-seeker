package com.elseeker.common.adapter.input.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class GlobalModelAttribute {

    @ModelAttribute("currentPath")
    fun currentPath(request: HttpServletRequest): String = request.requestURI
}
