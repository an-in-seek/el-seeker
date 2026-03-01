package com.elseeker.game.adapter.input.api.admin.request

import com.elseeker.game.domain.vo.QuizDifficulty

data class AdminWordPuzzleRequest(
    val title: String,
    val themeCode: String,
    val difficultyCode: QuizDifficulty,
    val boardWidth: Int,
    val boardHeight: Int,
)
