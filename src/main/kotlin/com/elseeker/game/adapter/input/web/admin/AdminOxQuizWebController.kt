package com.elseeker.game.adapter.input.web.admin

import com.elseeker.game.application.service.AdminOxQuizService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/web/admin/ox-quiz")
class AdminOxQuizWebController(
    private val adminOxQuizService: AdminOxQuizService,
) {
    @GetMapping("/stages")
    fun stageList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by("stageNumber"))
        val result = adminOxQuizService.findStages(pageable)
        val counts = adminOxQuizService.getQuestionCountsByStage()
        model.addAttribute("page", result)
        model.addAttribute("questionCounts", counts)
        return "admin/game/admin-ox-stage-list"
    }

    @GetMapping("/stages/new")
    fun stageNewForm(): String = "admin/game/admin-ox-stage-form"

    @GetMapping("/stages/{id}/edit")
    fun stageEditForm(@PathVariable id: Long, model: Model): String {
        model.addAttribute("stage", adminOxQuizService.findStageById(id))
        return "admin/game/admin-ox-stage-form"
    }

    @GetMapping("/stages/{stageId}/questions")
    fun questionList(
        @PathVariable stageId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by("orderIndex"))
        val result = adminOxQuizService.findQuestionsByStage(stageId, pageable)
        model.addAttribute("page", result)
        model.addAttribute("stage", adminOxQuizService.findStageById(stageId))
        return "admin/game/admin-ox-question-list"
    }

    @GetMapping("/stages/{stageId}/questions/new")
    fun questionNewForm(@PathVariable stageId: Long, model: Model): String {
        model.addAttribute("stage", adminOxQuizService.findStageById(stageId))
        return "admin/game/admin-ox-question-form"
    }

    @GetMapping("/questions/{id}/edit")
    fun questionEditForm(@PathVariable id: Long, model: Model): String {
        val question = adminOxQuizService.findQuestionById(id)
        model.addAttribute("question", question)
        model.addAttribute("stage", question.stage)
        return "admin/game/admin-ox-question-form"
    }
}
