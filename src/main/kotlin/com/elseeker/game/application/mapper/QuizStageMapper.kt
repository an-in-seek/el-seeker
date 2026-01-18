package com.elseeker.game.application.mapper

import com.elseeker.game.adapter.input.api.dto.QuizQuestionResponse
import com.elseeker.game.adapter.input.api.dto.QuizStageContextResponse
import com.elseeker.game.adapter.input.api.dto.QuizStageResponse
import com.elseeker.game.domain.model.QuizQuestion
import com.elseeker.game.domain.model.QuizStage

fun QuizStage.toResponse(stageCount: Int, context: QuizStageContextResponse): QuizStageResponse {
    val sortedQuestions = questions.sortedBy { it.id }
    return QuizStageResponse(
        stage = stageNumber,
        title = title,
        questions = sortedQuestions.map { it.toResponse() },
        stageCount = stageCount,
        questionCount = sortedQuestions.size,
        context = context
    )
}

private fun QuizQuestion.toResponse(): QuizQuestionResponse {
    return QuizQuestionResponse(
        id = requireNotNull(id) { "Quiz question id is null" },
        question = questionText,
        options = options.sortedBy { it.optionIndex }.map { it.optionText }
    )
}
