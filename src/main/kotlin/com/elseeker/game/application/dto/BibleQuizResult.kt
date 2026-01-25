package com.elseeker.game.application.dto

data class QuizStageDetailResult(
    val stageNumber: Int,
    val questions: List<QuizStageQuestionSnapshot>,
    val questionCount: Int,
    val progress: QuizStageProgressSnapshot
)

data class QuizStageQuestionSnapshot(
    val id: Long,
    val question: String,
    val options: List<String>
)

data class QuizStageProgressSnapshot(
    val stageNumber: Int,
    val currentStage: Int,
    val lastCompletedStage: Int,
    val isCompleted: Boolean,
    val isReviewOnly: Boolean,
    val isBlocked: Boolean,
    val currentQuestionIndex: Int,
    val currentScore: Int,
    val currentReviewType: String?,
    val lastScore: Int?,
    val reviewCount: Int,
    val hasInProgress: Boolean
)

data class QuizStageAnswerSnapshot(
    val isCorrect: Boolean,
    val correctIndex: Int,
    val currentScore: Int,
    val currentQuestionIndex: Int
)

data class QuizStageCompleteSnapshot(
    val nextStage: Int,
    val accuracy: Int?,
    val reviewCount: Int,
    val lastScore: Int?
)

data class QuizStageSummarySnapshot(
    val stageNumber: Int,
    val questionCount: Int,
    val status: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    val isLocked: Boolean,
    val lastScore: Int?,
    val accuracy: Int?,
    val reviewCount: Int,
    val hasInProgress: Boolean
)

data class QuizStageSummaryMapResult(
    val currentStage: Int,
    val lastCompletedStage: Int,
    val totalStages: Int,
    val stages: List<QuizStageSummarySnapshot>
)
