package com.elseeker.game.adapter.input.api.admin.request

import com.elseeker.game.domain.vo.QuizDifficulty

data class AdminOxQuestionRequest(
    val questionText: String,
    val correctAnswer: Boolean,
    val difficulty: QuizDifficulty,
    val orderIndex: Int
)
