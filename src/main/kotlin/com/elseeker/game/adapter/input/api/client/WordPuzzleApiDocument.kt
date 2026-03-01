package com.elseeker.game.adapter.input.api.client

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.client.request.*
import com.elseeker.game.adapter.input.api.client.response.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Word Puzzle", description = "성경 가로세로 낱말 퍼즐 API")
interface WordPuzzleApiDocument {

    @Operation(summary = "퍼즐 목록 조회", description = "게시된 퍼즐 목록과 이어하기 가능 여부를 반환합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요")
    )
    fun getPuzzles(
        @Parameter(description = "테마 코드 필터") theme: String?,
        @Parameter(description = "난이도 코드 필터") difficulty: String?,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") page: Int,
        @Parameter(description = "페이지 크기", example = "20") size: Int,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<Page<PuzzleSummaryResponse>>

    @Operation(summary = "퍼즐 시작 (신규)", description = "새로운 attempt를 생성하고 보드 구조와 단서 목록을 반환합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "시작 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "퍼즐을 찾을 수 없음"),
        ApiResponse(responseCode = "409", description = "이미 진행 중인 퍼즐이 있음")
    )
    fun startPuzzle(
        @Parameter(description = "퍼즐 ID", example = "1") puzzleId: Long,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<PuzzleAttemptResponse>

    @Operation(summary = "퍼즐 이어하기", description = "진행 중인 attempt의 저장된 상태를 복원하여 반환합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "퍼즐 시도를 찾을 수 없음")
    )
    fun resumeAttempt(
        @Parameter(description = "퍼즐 ID", example = "1") puzzleId: Long,
        @Parameter(description = "attempt ID", example = "42") attemptId: Long,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<PuzzleAttemptResponse>

    @Operation(summary = "셀 저장 (자동 저장)", description = "글자 입력/삭제 시 변경된 셀들을 일괄 저장합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "저장 성공"),
        ApiResponse(responseCode = "400", description = "이미 완료된 퍼즐"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "퍼즐 시도를 찾을 수 없음")
    )
    fun saveCells(
        @Parameter(description = "퍼즐 ID", example = "1") puzzleId: Long,
        @Parameter(description = "attempt ID", example = "42") attemptId: Long,
        @Valid @RequestBody request: CellSaveRequest,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<Void>

    @Operation(summary = "힌트 — 글자 공개", description = "선택한 칸의 정답 글자를 공개합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "공개 성공"),
        ApiResponse(responseCode = "400", description = "이미 공개된 칸 또는 완료된 퍼즐"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "퍼즐 시도 또는 항목을 찾을 수 없음")
    )
    fun revealLetter(
        @Parameter(description = "퍼즐 ID", example = "1") puzzleId: Long,
        @Parameter(description = "attempt ID", example = "42") attemptId: Long,
        @Valid @RequestBody request: RevealLetterRequest,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<RevealLetterResponse>

    @Operation(summary = "힌트 — 단어 확인", description = "현재 단어의 입력된 글자가 맞는지 셀별로 확인합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "확인 성공"),
        ApiResponse(responseCode = "400", description = "이미 완료된 퍼즐"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "퍼즐 시도 또는 항목을 찾을 수 없음")
    )
    fun checkWord(
        @Parameter(description = "퍼즐 ID", example = "1") puzzleId: Long,
        @Parameter(description = "attempt ID", example = "42") attemptId: Long,
        @Valid @RequestBody request: CheckWordRequest,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<CheckWordResponse>

    @Operation(summary = "전체 제출", description = "모든 셀의 정답을 검증하고 결과를 반환합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "제출 성공"),
        ApiResponse(responseCode = "400", description = "빈 칸이 존재하거나 완료된 퍼즐"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "퍼즐 시도를 찾을 수 없음")
    )
    fun submit(
        @Parameter(description = "퍼즐 ID", example = "1") puzzleId: Long,
        @Parameter(description = "attempt ID", example = "42") attemptId: Long,
        @Valid @RequestBody request: SubmitRequest,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<Any>
}
