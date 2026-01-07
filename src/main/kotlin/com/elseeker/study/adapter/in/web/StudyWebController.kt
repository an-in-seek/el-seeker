package com.elseeker.study.adapter.`in`.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/web/study")
class StudyWebController {

    @GetMapping
    fun showStudyHome(): String {
        return "study/study"
    }
}