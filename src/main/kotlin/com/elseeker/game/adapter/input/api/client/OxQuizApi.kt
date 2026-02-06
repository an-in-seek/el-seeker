package com.elseeker.game.adapter.input.api.client

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.client.request.OxQuizAnswerRequest
import com.elseeker.game.adapter.input.api.client.response.*
import com.elseeker.game.application.service.OxQuizService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/v1/game/bible-ox-quiz")
class OxQuizApi(
    private val oxQuizService: OxQuizService
) : OxQuizApiDocument {

    @GetMapping("/stages")
    override fun getStages(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<OxStageListResponse> {
        val response = oxQuizService.getStages(principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/stages/{stageNumber}")
    override fun getStage(
        @PathVariable stageNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<OxStageResponse> {
        val response = oxQuizService.getStage(stageNumber, principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/stages/{stageNumber}/start")
    override fun startStage(
        @PathVariable stageNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<OxStageStartResponse> {
        val response = oxQuizService.startStage(stageNumber, principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/stages/{stageNumber}/questions/{questionId}/answer")
    override fun submitAnswer(
        @PathVariable stageNumber: Int,
        @PathVariable questionId: Long,
        @Valid @RequestBody request: OxQuizAnswerRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<OxAnswerResponse> {
        val response = oxQuizService.submitAnswer(stageNumber, questionId, request, principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/stages/{stageNumber}/complete")
    override fun completeStage(
        @PathVariable stageNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<OxCompleteResponse> {
        val response = oxQuizService.completeStage(stageNumber, principal.memberUid)
        return ResponseEntity.ok(response)
    }
}
