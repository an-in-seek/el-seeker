package com.elseeker.game.adapter.input.api.client

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.client.mapper.toResponse
import com.elseeker.game.adapter.input.api.client.request.QuizStageAnswerRequest
import com.elseeker.game.adapter.input.api.client.request.QuizStageCompleteRequest
import com.elseeker.game.adapter.input.api.client.request.QuizStageStartRequest
import com.elseeker.game.adapter.input.api.client.response.*
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
    ): ResponseEntity<QuizStageResponse> {
        val response = bibleQuizService.getStage(stageNumber, principal.memberUid).toResponse()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/stages")
    override fun getStages(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<QuizStageMapResponse> {
        val response = bibleQuizService.getStageSummaries(principal.memberUid).toResponse()
        return ResponseEntity.ok(response)
    }

    @PostMapping("/stages/{stageNumber}/start")
    override fun startStage(
        @PathVariable stageNumber: Int,
        @Valid @RequestBody request: QuizStageStartRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<QuizStageProgressResponse> {
        val response = bibleQuizService.startStage(stageNumber, request, principal.memberUid).toResponse()
        return ResponseEntity.ok(response)
    }

    @PostMapping("/stages/{stageNumber}/answer")
    override fun submitAnswer(
        @PathVariable stageNumber: Int,
        @Valid @RequestBody request: QuizStageAnswerRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<QuizStageAnswerResponse> {
        val response = bibleQuizService.submitAnswer(stageNumber, request, principal.memberUid).toResponse()
        return ResponseEntity.ok(response)
    }

    @PostMapping("/stages/{stageNumber}/complete")
    override fun completeStage(
        @PathVariable stageNumber: Int,
        @Valid @RequestBody request: QuizStageCompleteRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<QuizStageCompleteResponse> {
        val response = bibleQuizService.completeStage(stageNumber, request, principal.memberUid).toResponse()
        return ResponseEntity.ok(response)
    }

    @PostMapping("/progress/reset")
    override fun resetProgress(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<Void> {
        bibleQuizService.resetProgress(principal.memberUid)
        return ResponseEntity.noContent().build()
    }
}
