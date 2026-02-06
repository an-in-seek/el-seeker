package com.elseeker.game.adapter.input.api.client.request

import com.elseeker.game.domain.vo.QuizStageAttemptMode
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import java.time.Instant

data class QuizStageStartRequest(
    @field:Schema(description = "진행 모드", example = "record")
    val mode: QuizStageAttemptMode,
    @field:Schema(description = "복습 유형", example = "full", nullable = true)
    val reviewType: String?,
    @field:Schema(description = "스테이지 시작 시각 (UTC)", example = "2024-01-01T10:00:00Z", nullable = true)
    val startedAt: Instant? = null
)

data class QuizStageAnswerRequest(
    @field:Schema(description = "문제 ID", example = "101")
    @field:Min(1)
    val questionId: Long,
    @field:Schema(description = "선택한 보기 인덱스", example = "2")
    @field:Min(0)
    val selectedIndex: Int,
    @field:Schema(description = "현재 문제 인덱스 (0-based)", example = "0")
    @field:Min(0)
    val questionIndex: Int,
    @field:Schema(description = "진행 모드", example = "record")
    val mode: QuizStageAttemptMode,
    @field:Schema(description = "정답 제출 시각 (UTC)", example = "2024-01-01T10:02:00Z", nullable = true)
    val answeredAt: Instant? = null
)

data class QuizStageCompleteRequest(
    @field:Schema(description = "진행 모드", example = "record")
    val mode: QuizStageAttemptMode,
    @field:Schema(description = "획득 점수", example = "7")
    @field:Min(0)
    val score: Int,
    @field:Schema(description = "총 문제 수", example = "10")
    @field:Min(0)
    val questionCount: Int,
    @field:Schema(description = "완료 시각 (UTC)", example = "2024-01-01T10:05:00Z", nullable = true)
    val completedAt: Instant? = null
)
