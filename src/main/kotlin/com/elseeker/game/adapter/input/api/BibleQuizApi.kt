package com.elseeker.game.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.request.QuizStageAnswerRequest
import com.elseeker.game.adapter.input.api.request.QuizStageCompleteRequest
import com.elseeker.game.adapter.input.api.request.QuizStageStartRequest
import com.elseeker.game.adapter.input.api.response.*
import com.elseeker.game.application.service.BibleQuizService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/v1/game/bible-quiz")
class BibleQuizApi(
    private val bibleQuizService: BibleQuizService
) : BibleQuizApiDocument {

    @GetMapping("/stages/{stageNumber}")
    override fun getStage(
        @PathVariable stageNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): QuizStageResponse {
        return bibleQuizService.getStage(stageNumber, principal.memberUid)
    }

    @GetMapping("/stages")
    override fun getStages(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): QuizStageMapResponse {
        return bibleQuizService.getStageSummaries(principal.memberUid)
    }

    @PostMapping("/stages/{stageNumber}/start")
    override fun startStage(
        @PathVariable stageNumber: Int,
        @Valid @RequestBody request: QuizStageStartRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): QuizStageProgressResponse {
        return bibleQuizService.startStage(stageNumber, request, principal.memberUid)
    }

    @PostMapping("/stages/{stageNumber}/answer")
    override fun submitAnswer(
        @PathVariable stageNumber: Int,
        @Valid @RequestBody request: QuizStageAnswerRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): QuizStageAnswerResponse {
        return bibleQuizService.submitAnswer(stageNumber, request, principal.memberUid)
    }

    @PostMapping("/stages/{stageNumber}/complete")
    override fun completeStage(
        @PathVariable stageNumber: Int,
        @Valid @RequestBody request: QuizStageCompleteRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): QuizStageCompleteResponse {
        return bibleQuizService.completeStage(stageNumber, request, principal.memberUid)
    }

    @PostMapping("/progress/reset")
    override fun resetProgress(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<Void> {
        bibleQuizService.resetProgress(principal.memberUid)
        return ResponseEntity.noContent().build()
    }
}
