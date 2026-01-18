package com.elseeker.game.adapter.input.api.response

data class QuizStageMapResponse(
    val currentStage: Int,
    val lastCompletedStage: Int,
    val totalStages: Int,
    val stages: List<QuizStageSummaryResponse>
)

data class QuizStageSummaryResponse(
    val stage: Int,
    val questionCount: Int,
    val status: String,
    val score: Int?,
    val accuracy: Int?,
    val reviewCount: Int
)

data class QuizStageAnswerResponse(
    val isCorrect: Boolean,
    val correctIndex: Int,
    val currentScore: Int
)

data class QuizStageCompleteResponse(
    val nextStage: Int,
    val accuracy: Int?,
    val reviewCount: Int
)
