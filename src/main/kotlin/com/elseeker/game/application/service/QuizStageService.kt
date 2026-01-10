package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.dto.QuizStageResponse
import com.elseeker.game.adapter.input.api.dto.QuizStageSummaryResponse
import com.elseeker.game.adapter.output.jpa.QuizStageRepository
import com.elseeker.game.application.mapper.toResponse
import org.springframework.stereotype.Service

@Service
class QuizStageService(
    private val quizStageRepository: QuizStageRepository
) {

    fun getStage(stageNumber: Int): QuizStageResponse {
        if (stageNumber < 1) throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
        val stage = quizStageRepository.findByStageNumber(stageNumber) ?: throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
        val stageCount = quizStageRepository.count().toInt()
        return stage.toResponse(stageCount)
    }

    fun getStageSummaries(): List<QuizStageSummaryResponse> {
        return quizStageRepository.findStageSummaries()
            .map { summary ->
                QuizStageSummaryResponse(
                    stage = summary.stageNumber,
                    questionCount = summary.questionCount.toInt()
                )
            }
    }
}
