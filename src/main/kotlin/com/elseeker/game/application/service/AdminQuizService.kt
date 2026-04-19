package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.output.jpa.QuizQuestionOptionRepository
import com.elseeker.game.adapter.output.jpa.QuizQuestionRepository
import com.elseeker.game.adapter.output.jpa.QuizStageRepository
import com.elseeker.game.domain.model.QuizQuestion
import com.elseeker.game.domain.model.QuizQuestionOption
import com.elseeker.game.domain.model.QuizStage
import com.elseeker.game.domain.vo.QuizDifficulty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminQuizService(
    private val stageRepository: QuizStageRepository,
    private val questionRepository: QuizQuestionRepository,
    private val optionRepository: QuizQuestionOptionRepository,
) {
    companion object {
        private const val MIN_OPTIONS = 2
        private const val MAX_OPTIONS = 4
    }

    fun findStages(pageable: Pageable): Page<QuizStage> = stageRepository.findAll(pageable)

    fun findStageById(id: Long): QuizStage =
        stageRepository.findByIdOrNull(id) ?: throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "id=$id")

    fun findQuestionsByStage(stageId: Long, pageable: Pageable): Page<QuizQuestion> =
        questionRepository.findByStageId(stageId, pageable)

    fun findQuestionById(id: Long): QuizQuestion =
        questionRepository.findWithOptionsById(id) ?: throwError(ErrorType.QUIZ_QUESTION_NOT_FOUND, "id=$id")

    fun getStageSummaries(): Map<Int, Long> =
        stageRepository.findStageSummaries().associate { it.stageNumber to it.questionCount }

    @Transactional
    fun createStage(stageNumber: Int, title: String?): QuizStage {
        if (stageNumber <= 0) {
            throwError(ErrorType.INVALID_PARAMETER, "stageNumber=$stageNumber")
        }
        if (stageRepository.existsByStageNumber(stageNumber)) {
            throwError(ErrorType.INVALID_PARAMETER, "stageNumber=$stageNumber")
        }
        return stageRepository.save(
            QuizStage(
                stageNumber = stageNumber,
                title = title?.takeIf { it.isNotBlank() }
            )
        )
    }

    @Transactional
    fun updateStage(id: Long, stageNumber: Int, title: String?): QuizStage {
        if (stageNumber <= 0) {
            throwError(ErrorType.INVALID_PARAMETER, "stageNumber=$stageNumber")
        }
        val existing = findStageById(id)
        if (existing.stageNumber != stageNumber && stageRepository.existsByStageNumber(stageNumber)) {
            throwError(ErrorType.INVALID_PARAMETER, "stageNumber=$stageNumber")
        }
        stageRepository.updateStage(id, stageNumber, title?.takeIf { it.isNotBlank() })
        return findStageById(id)
    }

    @Transactional
    fun deleteStage(id: Long) {
        val stage = findStageById(id)
        stageRepository.delete(stage)
    }

    @Transactional
    fun createQuestion(
        stageId: Long,
        questionText: String,
        answerIndex: Int,
        difficulty: QuizDifficulty?,
        options: List<OptionPayload>
    ): QuizQuestion {
        val stage = findStageById(stageId)
        validateOptions(options, answerIndex)
        val question = QuizQuestion(
            stage = stage,
            questionText = questionText,
            answerIndex = answerIndex,
            difficulty = difficulty
        )
        options.forEach { opt ->
            question.addOption(
                QuizQuestionOption(
                    question = question,
                    optionText = opt.optionText,
                    optionIndex = opt.optionIndex
                )
            )
        }
        return questionRepository.save(question)
    }

    @Transactional
    fun updateQuestion(
        id: Long,
        questionText: String,
        answerIndex: Int,
        difficulty: QuizDifficulty?,
        options: List<OptionPayload>
    ): QuizQuestion {
        val existing = findQuestionById(id)
        validateOptions(options, answerIndex)
        optionRepository.deleteByQuestionId(id)
        val updated = QuizQuestion(
            id = existing.id,
            stage = existing.stage,
            questionText = questionText,
            answerIndex = answerIndex,
            difficulty = difficulty
        )
        options.forEach { opt ->
            updated.addOption(
                QuizQuestionOption(
                    question = updated,
                    optionText = opt.optionText,
                    optionIndex = opt.optionIndex
                )
            )
        }
        return questionRepository.save(updated)
    }

    @Transactional
    fun deleteQuestion(id: Long) {
        val question = findQuestionById(id)
        questionRepository.delete(question)
    }

    private fun validateOptions(options: List<OptionPayload>, answerIndex: Int) {
        if (options.size < MIN_OPTIONS) {
            throwError(ErrorType.INVALID_PARAMETER, "options.size")
        }
        if (options.size > MAX_OPTIONS) {
            throwError(ErrorType.INVALID_PARAMETER, "options.size")
        }
        val optionIndexes = options.map { it.optionIndex }
        if (optionIndexes.any { it !in 0..<MAX_OPTIONS }) {
            throwError(ErrorType.INVALID_PARAMETER, "optionIndex")
        }
        if (optionIndexes.distinct().size != optionIndexes.size) {
            throwError(ErrorType.INVALID_PARAMETER, "optionIndex")
        }
        if (!optionIndexes.contains(answerIndex)) {
            throwError(ErrorType.INVALID_PARAMETER, "answerIndex=$answerIndex")
        }
        if (options.any { it.optionText.isBlank() }) {
            throwError(ErrorType.INVALID_PARAMETER, "optionText")
        }
    }

    data class OptionPayload(
        val optionIndex: Int,
        val optionText: String
    )
}
