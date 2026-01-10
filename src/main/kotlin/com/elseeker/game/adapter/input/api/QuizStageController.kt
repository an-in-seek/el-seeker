package com.elseeker.game.adapter.input.api

import com.elseeker.game.adapter.input.api.dto.QuizStageResponse
import com.elseeker.game.adapter.input.api.dto.QuizStageSummaryResponse
import com.elseeker.game.application.service.QuizStageService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/game/bible-quiz")
class QuizStageController(
    private val quizStageService: QuizStageService
) {

    @GetMapping("/stages/{stageNumber}")
    fun getStage(@PathVariable stageNumber: Int): QuizStageResponse {
        return quizStageService.getStage(stageNumber)
    }

    @GetMapping("/stages")
    fun getStages(): List<QuizStageSummaryResponse> {
        return quizStageService.getStageSummaries()
    }
}
