package com.elseeker.game.application.mapper

import com.elseeker.game.adapter.input.api.response.QuizQuestionResponse
import com.elseeker.game.adapter.input.api.response.QuizStageProgressResponse
import com.elseeker.game.adapter.input.api.response.QuizStageResponse
import com.elseeker.game.domain.model.QuizQuestion
import com.elseeker.game.domain.model.QuizStage

fun QuizStage.toResponse(stageCount: Int, progress: QuizStageProgressResponse): QuizStageResponse {
    val sortedQuestions = questions.sortedBy { it.id }
    return QuizStageResponse(
        stageNumber = stageNumber,
        title = title,
        questions = sortedQuestions.map { it.toResponse() },
        stageCount = stageCount,
        questionCount = sortedQuestions.size,
        progress = progress
    )
}

private fun QuizQuestion.toResponse(): QuizQuestionResponse {
    return QuizQuestionResponse(
        id = requireNotNull(id) { "Quiz question id is null" },
        question = questionText,
        options = options.sortedBy { it.optionIndex }.map { it.optionText }
    )
}
