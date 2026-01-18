package com.elseeker.game.adapter.input.api.dto

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

data class QuizStageStartRequest(
    val mode: String,
    val reviewType: String?
)

data class QuizStageAnswerRequest(
    val questionId: Long,
    val selectedIndex: Int,
    val questionIndex: Int,
    val mode: String
)

data class QuizStageAnswerResponse(
    val isCorrect: Boolean,
    val correctIndex: Int,
    val currentScore: Int
)

data class QuizStageCompleteRequest(
    val mode: String,
    val score: Int,
    val questionCount: Int
)

data class QuizStageCompleteResponse(
    val nextStage: Int,
    val accuracy: Int?,
    val reviewCount: Int
)
