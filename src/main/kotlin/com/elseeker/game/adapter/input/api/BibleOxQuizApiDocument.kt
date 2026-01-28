package com.elseeker.game.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.request.BibleOxQuizAnswerRequest
import com.elseeker.game.adapter.input.api.response.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Bible O/X Quiz", description = "성경 O/X 퀴즈 API")
interface BibleOxQuizApiDocument {

    @Operation(summary = "스테이지 목록 조회", description = "전체 66개 스테이지의 요약 정보와 진행 현황을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요")
    )
    fun getStages(
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<BibleOxStageListResponse>

    @Operation(summary = "스테이지 상세 조회", description = "특정 스테이지의 문제 목록을 조회합니다. 정답은 포함되지 않습니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 스테이지 번호"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "스테이지를 찾을 수 없음")
    )
    fun getStage(
        @Parameter(description = "스테이지 번호 (1~66)", example = "1") stageNumber: Int,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<BibleOxStageResponse>

    @Operation(summary = "스테이지 시작", description = "스테이지 플레이를 시작합니다. 진행 중인 시도가 있으면 재사용합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "시작 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 스테이지 번호"),
        ApiResponse(responseCode = "401", description = "인증 필요")
    )
    fun startStage(
        @Parameter(description = "스테이지 번호 (1~66)", example = "1") stageNumber: Int,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<BibleOxStageStartResponse>

    @Operation(summary = "답안 제출", description = "문제에 대한 답안을 제출하고 정답 여부를 확인합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "제출 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청 (스테이지/문제 불일치, 이미 답변한 문제 등)"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "진행 중인 시도 또는 문제를 찾을 수 없음")
    )
    fun submitAnswer(
        @Parameter(description = "스테이지 번호 (1~66)", example = "1") stageNumber: Int,
        @Parameter(description = "문제 ID", example = "101") questionId: Long,
        @Valid @RequestBody request: BibleOxQuizAnswerRequest,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<BibleOxAnswerResponse>

    @Operation(summary = "스테이지 완료", description = "스테이지를 완료하고 최종 점수를 확정합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "완료 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 스테이지 번호"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "진행 중인 시도를 찾을 수 없음")
    )
    fun completeStage(
        @Parameter(description = "스테이지 번호 (1~66)", example = "1") stageNumber: Int,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<BibleOxCompleteResponse>
}
