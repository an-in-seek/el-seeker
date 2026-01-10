package com.elseeker.game.application.service

import com.elseeker.game.adapter.input.api.dto.QuizStageResponse
import com.elseeker.game.adapter.input.api.dto.QuizStageSummaryResponse
import com.elseeker.game.adapter.output.jpa.QuizStageRepository
import com.elseeker.game.application.mapper.toResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class QuizStageService(
    private val quizStageRepository: QuizStageRepository
) {

    fun getStage(stageNumber: Int): QuizStageResponse {
        if (stageNumber < 1) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found")
        }
        val stage = quizStageRepository.findByStageNumber(stageNumber)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found")
        return stage.toResponse()
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
