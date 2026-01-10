package com.elseeker.game.adapter.input.api.dto

data class QuizStageResponse(
    val stage: Int,
    val title: String?,
    val questions: List<QuizQuestionResponse>,
    val stageCount: Int,
    val questionCount: Int
)

data class QuizQuestionResponse(
    val id: Long,
    val question: String,
    val options: List<String>,
    val answerIndex: Int
)

data class QuizStageSummaryResponse(
    val stage: Int,
    val questionCount: Int
)
