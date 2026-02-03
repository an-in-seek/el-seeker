package com.elseeker.game.adapter.input.api.admin.request

import com.elseeker.game.domain.vo.QuizDifficulty

data class AdminQuizQuestionRequest(
    val questionText: String,
    val answerIndex: Int,
    val difficulty: QuizDifficulty?,
    val options: List<AdminQuizOptionRequest>
)

data class AdminQuizOptionRequest(
    val optionIndex: Int,
    val optionText: String
)
