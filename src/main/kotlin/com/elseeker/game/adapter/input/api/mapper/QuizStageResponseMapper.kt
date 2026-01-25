package com.elseeker.game.adapter.input.api.mapper

import com.elseeker.game.adapter.input.api.response.*
import com.elseeker.game.application.dto.QuizStageAnswerSnapshot
import com.elseeker.game.application.dto.QuizStageCompleteSnapshot
import com.elseeker.game.application.dto.QuizStageDetailResult
import com.elseeker.game.application.dto.QuizStageProgressSnapshot
import com.elseeker.game.application.dto.QuizStageQuestionSnapshot
import com.elseeker.game.application.dto.QuizStageSummaryMapResult
import com.elseeker.game.application.dto.QuizStageSummarySnapshot

fun QuizStageDetailResult.toResponse(): QuizStageResponse {
    return QuizStageResponse(
        stageNumber = stageNumber,
        title = title,
        questions = questions.map { it.toResponse() },
        stageCount = stageCount,
        questionCount = questionCount,
        progress = progress.toResponse()
    )
}

fun QuizStageProgressSnapshot.toResponse(): QuizStageProgressResponse {
    return QuizStageProgressResponse(
        stageNumber = stageNumber,
        currentStage = currentStage,
        lastCompletedStage = lastCompletedStage,
        isCompleted = isCompleted,
        isReviewOnly = isReviewOnly,
        isBlocked = isBlocked,
        currentQuestionIndex = currentQuestionIndex,
        currentScore = currentScore,
        currentReviewType = currentReviewType,
        lastScore = lastScore,
        reviewCount = reviewCount,
        hasInProgress = hasInProgress
    )
}

fun QuizStageAnswerSnapshot.toResponse(): QuizStageAnswerResponse {
    return QuizStageAnswerResponse(
        isCorrect = isCorrect,
        correctIndex = correctIndex,
        currentScore = currentScore,
        currentQuestionIndex = currentQuestionIndex
    )
}

fun QuizStageCompleteSnapshot.toResponse(): QuizStageCompleteResponse {
    return QuizStageCompleteResponse(
        nextStage = nextStage,
        accuracy = accuracy,
        reviewCount = reviewCount,
        lastScore = lastScore
    )
}

fun QuizStageSummaryMapResult.toResponse(): QuizStageMapResponse {
    return QuizStageMapResponse(
        currentStage = currentStage,
        lastCompletedStage = lastCompletedStage,
        totalStages = totalStages,
        stages = stages.map { it.toResponse() }
    )
}

private fun QuizStageSummarySnapshot.toResponse(): QuizStageSummaryResponse {
    return QuizStageSummaryResponse(
        stageNumber = stageNumber,
        questionCount = questionCount,
        status = status,
        isCompleted = isCompleted,
        isCurrent = isCurrent,
        isLocked = isLocked,
        lastScore = lastScore,
        accuracy = accuracy,
        reviewCount = reviewCount,
        hasInProgress = hasInProgress
    )
}

private fun QuizStageQuestionSnapshot.toResponse(): QuizQuestionResponse {
    return QuizQuestionResponse(
        id = id,
        question = question,
        options = options
    )
}
