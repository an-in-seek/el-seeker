package com.elseeker.game.application.mapper

import com.elseeker.game.adapter.input.api.dto.QuizQuestionResponse
import com.elseeker.game.adapter.input.api.dto.QuizStageResponse
import com.elseeker.game.domain.model.QuizQuestion
import com.elseeker.game.domain.model.QuizStage

fun QuizStage.toResponse(): QuizStageResponse {
    return QuizStageResponse(
        stage = stageNumber,
        title = title,
        questions = questions
            .sortedBy { it.id }
            .map { it.toResponse() }
    )
}

private fun QuizQuestion.toResponse(): QuizQuestionResponse {
    return QuizQuestionResponse(
        id = requireNotNull(id) { "Quiz question id is null" },
        question = questionText,
        options = options.sortedBy { it.optionIndex }.map { it.optionText },
        answerIndex = answerIndex
    )
}
