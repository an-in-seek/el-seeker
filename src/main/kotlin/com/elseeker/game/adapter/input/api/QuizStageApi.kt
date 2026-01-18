package com.elseeker.game.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.request.QuizStageAnswerRequest
import com.elseeker.game.adapter.input.api.request.QuizStageCompleteRequest
import com.elseeker.game.adapter.input.api.request.QuizStageStartRequest
import com.elseeker.game.adapter.input.api.response.QuizStageAnswerResponse
import com.elseeker.game.adapter.input.api.response.QuizStageCompleteResponse
import com.elseeker.game.adapter.input.api.response.QuizStageContextResponse
import com.elseeker.game.adapter.input.api.response.QuizStageMapResponse
import com.elseeker.game.adapter.input.api.response.QuizStageResponse
import com.elseeker.game.application.service.QuizStageService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/game/bible-quiz")
class QuizStageApi(
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
