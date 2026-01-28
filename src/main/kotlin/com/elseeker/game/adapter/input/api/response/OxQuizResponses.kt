package com.elseeker.game.adapter.input.api.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "O/X 퀴즈 스테이지 상세 응답")
data class OxStageResponse(
    @field:Schema(description = "스테이지 번호", example = "1")
    val stageNumber: Int,

    @field:Schema(description = "성경 책 이름", example = "창세기")
    val bookName: String,

    @field:Schema(description = "총 문제 수", example = "10")
    val totalQuestions: Int,

    @field:Schema(description = "문제 목록")
    val questions: List<OxQuestionResponse>
)

@Schema(description = "O/X 퀴즈 문제 응답 (정답 미포함)")
data class OxQuestionResponse(
    @field:Schema(description = "문제 ID", example = "101")
    val questionId: Long,

    @field:Schema(description = "문제 텍스트", example = "아담은 에덴동산에서 창조되었다.")
    val questionText: String,

    @field:Schema(description = "문제 순서 (1~10)", example = "1")
    val orderIndex: Int
)

@Schema(description = "O/X 퀴즈 스테이지 시작 응답")
data class OxStageStartResponse(
    @field:Schema(description = "스테이지 시도 ID", example = "1")
    val stageAttemptId: Long,

    @field:Schema(description = "스테이지 번호", example = "1")
    val stageNumber: Int,

    @field:Schema(description = "시작 시간 (UTC)", example = "2024-01-15T10:30:00Z")
    val startedAt: Instant,

    @field:Schema(description = "현재 점수", example = "0")
    val currentScore: Int,

    @field:Schema(description = "답변한 문제 수", example = "0")
    val answeredCount: Int
)

@Schema(description = "O/X 퀴즈 답안 제출 응답")
data class OxAnswerResponse(
    @field:Schema(description = "정답 여부", example = "true")
    val isCorrect: Boolean,

    @field:Schema(description = "정답", example = "true")
    val correctAnswer: Boolean,

    @field:Schema(description = "현재 누적 점수", example = "5")
    val currentScore: Int,

    @field:Schema(description = "답변 시간 (UTC)", example = "2024-01-15T10:31:00Z")
    val answeredAt: Instant
)

@Schema(description = "O/X 퀴즈 스테이지 완료 응답")
data class OxCompleteResponse(
    @field:Schema(description = "최종 점수", example = "8")
    val score: Int,

    @field:Schema(description = "총 문제 수", example = "10")
    val totalQuestions: Int,

    @field:Schema(description = "정답률 (%)", example = "80")
    val accuracyPercent: Int,

    @field:Schema(description = "완료 시간 (UTC)", example = "2024-01-15T10:35:00Z")
    val completedAt: Instant
)

@Schema(description = "O/X 퀴즈 스테이지 목록 응답")
data class OxStageListResponse(
    @field:Schema(description = "총 스테이지 수", example = "66")
    val totalStages: Int,

    @field:Schema(description = "스테이지 요약 목록")
    val stages: List<OxStageSummaryResponse>
)

@Schema(description = "O/X 퀴즈 스테이지 요약 응답")
data class OxStageSummaryResponse(
    @field:Schema(description = "스테이지 번호", example = "1")
    val stageNumber: Int,

    @field:Schema(description = "성경 책 이름", example = "창세기")
    val bookName: String,

    @field:Schema(description = "총 문제 수", example = "10")
    val totalQuestions: Int,

    @field:Schema(description = "완료 여부", example = "true")
    val isCompleted: Boolean,

    @field:Schema(description = "최고 점수 (완료한 경우)", example = "9")
    val bestScore: Int?,

    @field:Schema(description = "진행 중 여부", example = "false")
    val hasInProgress: Boolean
)
