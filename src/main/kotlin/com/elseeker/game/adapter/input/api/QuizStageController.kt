package com.elseeker.game.adapter.input.api

import com.elseeker.game.adapter.input.api.dto.*
import com.elseeker.game.application.service.QuizStageService
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/game/bible-quiz")
class QuizStageController(
    private val quizStageService: QuizStageService
) {

    @GetMapping("/stages/{stageNumber}")
    fun getStage(
        @PathVariable stageNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): QuizStageResponse {
        return quizStageService.getStage(stageNumber, principal.memberUid)
    }

    @GetMapping("/stages")
    fun getStages(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): QuizStageMapResponse {
        return quizStageService.getStageSummaries(principal.memberUid)
    }

    @PostMapping("/stages/{stageNumber}/start")
    fun startStage(
        @PathVariable stageNumber: Int,
        @RequestBody request: QuizStageStartRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): QuizStageContextResponse {
        return quizStageService.startStage(stageNumber, request, principal.memberUid)
    }

    @PostMapping("/stages/{stageNumber}/answer")
    fun submitAnswer(
        @PathVariable stageNumber: Int,
        @RequestBody request: QuizStageAnswerRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): QuizStageAnswerResponse {
        return quizStageService.submitAnswer(stageNumber, request, principal.memberUid)
    }

    @PostMapping("/stages/{stageNumber}/complete")
    fun completeStage(
        @PathVariable stageNumber: Int,
        @RequestBody request: QuizStageCompleteRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): QuizStageCompleteResponse {
        return quizStageService.completeStage(stageNumber, request, principal.memberUid)
    }

    @PostMapping("/progress/reset")
    fun resetProgress(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<Void> {
        quizStageService.resetProgress(principal.memberUid)
        return ResponseEntity.noContent().build()
    }
}
