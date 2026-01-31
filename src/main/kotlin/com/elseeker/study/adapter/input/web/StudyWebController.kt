package com.elseeker.study.adapter.input.web

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

    @GetMapping("/bible-overview-video")
    fun showBibleOverviewVideo(): String {
        return "study/bible-overview-video"
    }

    @GetMapping("/bible-genealogy")
    fun showBibleGenealogy(): String {
        return "study/bible-genealogy"
    }
}