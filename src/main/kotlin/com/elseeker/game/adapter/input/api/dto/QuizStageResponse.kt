package com.elseeker.game.adapter.input.api.dto

data class QuizStageResponse(
    val stage: Int,
    val title: String?,
    val questions: List<QuizQuestionResponse>,
    val stageCount: Int,
    val questionCount: Int,
    val context: QuizStageContextResponse
)

data class QuizQuestionResponse(
    val id: Long,
    val question: String,
    val options: List<String>
)

data class QuizStageContextResponse(
    val activeStage: Int,
    val boundedCurrentStage: Int,
    val currentStage: Int,
    val lastCompletedStage: Int,
    val isCompletedStage: Boolean,
    val isReviewOnly: Boolean,
    val isBlocked: Boolean,
    val currentQuestionIndex: Int,
    val currentScore: Int,
    val reviewType: String?,
    val hasInProgress: Boolean
)
