package com.elseeker.game.adapter.input.api.request

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

data class QuizStageCompleteRequest(
    val mode: String,
    val score: Int,
    val questionCount: Int
)
