package com.elseeker.game.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.request.BibleOxQuizAnswerRequest
import com.elseeker.game.adapter.input.api.response.*
import com.elseeker.game.application.service.BibleOxQuizService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/v1/game/bible-ox-quiz")
class BibleOxQuizApi(
    private val bibleOxQuizService: BibleOxQuizService
) : BibleOxQuizApiDocument {

    @GetMapping("/stages")
    override fun getStages(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<BibleOxStageListResponse> {
        val response = bibleOxQuizService.getStages(principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/stages/{stageNumber}")
    override fun getStage(
        @PathVariable stageNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<BibleOxStageResponse> {
        val response = bibleOxQuizService.getStage(stageNumber, principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/stages/{stageNumber}/start")
    override fun startStage(
        @PathVariable stageNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<BibleOxStageStartResponse> {
        val response = bibleOxQuizService.startStage(stageNumber, principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/stages/{stageNumber}/questions/{questionId}/answer")
    override fun submitAnswer(
        @PathVariable stageNumber: Int,
        @PathVariable questionId: Long,
        @Valid @RequestBody request: BibleOxQuizAnswerRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<BibleOxAnswerResponse> {
        val response = bibleOxQuizService.submitAnswer(stageNumber, questionId, request, principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/stages/{stageNumber}/complete")
    override fun completeStage(
        @PathVariable stageNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<BibleOxCompleteResponse> {
        val response = bibleOxQuizService.completeStage(stageNumber, principal.memberUid)
        return ResponseEntity.ok(response)
    }
}
