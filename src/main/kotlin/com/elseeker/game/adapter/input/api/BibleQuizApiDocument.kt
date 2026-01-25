package com.elseeker.game.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.request.QuizStageAnswerRequest
import com.elseeker.game.adapter.input.api.request.QuizStageCompleteRequest
import com.elseeker.game.adapter.input.api.request.QuizStageStartRequest
import com.elseeker.game.adapter.input.api.response.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "Bible Quiz", description = "Bible Quiz APIs")
interface BibleQuizApiDocument {

    @Operation(summary = "Get stage detail")
    fun getStage(
        @Parameter(description = "스테이지 번호", example = "1")
        stageNumber: Int,
        principal: JwtPrincipal
    ): QuizStageResponse

    @Operation(summary = "Get stage summaries")
    fun getStages(
        principal: JwtPrincipal
    ): QuizStageMapResponse

    @Operation(summary = "Start stage")
    fun startStage(
        @Parameter(description = "스테이지 번호", example = "1")
        stageNumber: Int,
        request: QuizStageStartRequest,
        principal: JwtPrincipal
    ): QuizStageProgressResponse

    @Operation(summary = "Submit answer")
    fun submitAnswer(
        @Parameter(description = "스테이지 번호", example = "1")
        stageNumber: Int,
        request: QuizStageAnswerRequest,
        principal: JwtPrincipal
    ): QuizStageAnswerResponse

    @Operation(summary = "Complete stage")
    fun completeStage(
        @Parameter(description = "스테이지 번호", example = "1")
        stageNumber: Int,
        request: QuizStageCompleteRequest,
        principal: JwtPrincipal
    ): QuizStageCompleteResponse

    @Operation(summary = "Reset progress")
    fun resetProgress(
        principal: JwtPrincipal
    ): ResponseEntity<Void>
}
