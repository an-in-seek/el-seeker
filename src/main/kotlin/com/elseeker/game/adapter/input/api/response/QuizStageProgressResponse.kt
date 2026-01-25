package com.elseeker.game.adapter.input.api.response

import io.swagger.v3.oas.annotations.media.Schema

data class QuizStageMapResponse(
    @field:Schema(description = "현재 진행 가능한 스테이지 번호", example = "2")
    val currentStage: Int,
    @field:Schema(description = "마지막 완료 스테이지 번호", example = "1")
    val lastCompletedStage: Int,
    @field:Schema(description = "전체 스테이지 수", example = "10")
    val totalStages: Int,
    val stages: List<QuizStageSummaryResponse>
)

data class QuizStageSummaryResponse(
    @field:Schema(description = "스테이지 번호", example = "1")
    val stageNumber: Int,
    @field:Schema(description = "문제 수", example = "10")
    val questionCount: Int,
    @field:Schema(description = "상태", example = "completed")
    val status: String,
    @field:Schema(description = "완료 여부", example = "true")
    val isCompleted: Boolean,
    @field:Schema(description = "현재 스테이지 여부", example = "false")
    val isCurrent: Boolean,
    @field:Schema(description = "잠김 여부", example = "false")
    val isLocked: Boolean,
    @field:Schema(description = "마지막 점수", example = "7", nullable = true)
    val lastScore: Int?,
    @field:Schema(description = "정답률(%)", example = "70", nullable = true)
    val accuracy: Int?,
    @field:Schema(description = "복습 횟수", example = "1")
    val reviewCount: Int,
    @field:Schema(description = "진행 중 여부", example = "false")
    val hasInProgress: Boolean
)

data class QuizStageProgressResponse(
    @field:Schema(description = "스테이지 번호", example = "1")
    val stageNumber: Int,
    @field:Schema(description = "현재 진행 가능한 스테이지 번호", example = "2")
    val currentStage: Int,
    @field:Schema(description = "마지막 완료 스테이지 번호", example = "1")
    val lastCompletedStage: Int,
    @field:Schema(description = "완료 여부", example = "true")
    val isCompleted: Boolean,
    @field:Schema(description = "복습 전용 여부", example = "true")
    val isReviewOnly: Boolean,
    @field:Schema(description = "잠김 여부", example = "false")
    val isBlocked: Boolean,
    @field:Schema(description = "현재 문제 인덱스(0-based)", example = "3")
    val currentQuestionIndex: Int,
    @field:Schema(description = "현재 점수", example = "2")
    val currentScore: Int,
    @field:Schema(description = "복습 유형", example = "full", nullable = true)
    val currentReviewType: String?,
    @field:Schema(description = "마지막 점수", example = "7", nullable = true)
    val lastScore: Int?,
    @field:Schema(description = "복습 횟수", example = "1")
    val reviewCount: Int,
    @field:Schema(description = "진행 중 여부", example = "true")
    val hasInProgress: Boolean
)

data class QuizStageAnswerResponse(
    @field:Schema(description = "정답 여부", example = "true")
    val isCorrect: Boolean,
    @field:Schema(description = "정답 인덱스", example = "1")
    val correctIndex: Int,
    @field:Schema(description = "현재 점수", example = "3")
    val currentScore: Int,
    @field:Schema(description = "다음 문제 인덱스(0-based)", example = "4")
    val currentQuestionIndex: Int
)

data class QuizStageCompleteResponse(
    @field:Schema(description = "다음 스테이지 번호", example = "2")
    val nextStage: Int,
    @field:Schema(description = "정답률(%)", example = "80", nullable = true)
    val accuracy: Int?,
    @field:Schema(description = "복습 횟수", example = "2")
    val reviewCount: Int,
    @field:Schema(description = "마지막 점수", example = "8", nullable = true)
    val lastScore: Int?
)
