package com.elseeker.game.adapter.input.web.admin

import com.elseeker.game.application.service.AdminQuizService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/web/admin/quiz")
class AdminQuizWebController(
    private val adminQuizService: AdminQuizService,
) {
    @GetMapping("/stages")
    fun stageList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by("stageNumber"))
        val result = adminQuizService.findStages(pageable)
        val summaries = adminQuizService.getStageSummaries()
        model.addAttribute("page", result)
        model.addAttribute("questionCounts", summaries)
        return "admin/game/admin-quiz-stage-list"
    }

    @GetMapping("/stages/new")
    fun stageNewForm(): String = "admin/game/admin-quiz-stage-form"

    @GetMapping("/stages/{id}/edit")
    fun stageEditForm(@PathVariable id: Long, model: Model): String {
        model.addAttribute("stage", adminQuizService.findStageById(id))
        return "admin/game/admin-quiz-stage-form"
    }

    @GetMapping("/stages/{stageId}/questions")
    fun questionList(
        @PathVariable stageId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        model: Model,
    ): String {
        val pageable = PageRequest.of(page, size, Sort.by("id"))
        val result = adminQuizService.findQuestionsByStage(stageId, pageable)
        model.addAttribute("page", result)
        model.addAttribute("stage", adminQuizService.findStageById(stageId))
        return "admin/game/admin-quiz-question-list"
    }

    @GetMapping("/stages/{stageId}/questions/new")
    fun questionNewForm(@PathVariable stageId: Long, model: Model): String {
        model.addAttribute("stage", adminQuizService.findStageById(stageId))
        return "admin/game/admin-quiz-question-form"
    }

    @GetMapping("/questions/{id}/edit")
    fun questionEditForm(@PathVariable id: Long, model: Model): String {
        val question = adminQuizService.findQuestionById(id)
        model.addAttribute("question", question)
        model.addAttribute("stage", question.stage)
        return "admin/game/admin-quiz-question-form"
    }
}
