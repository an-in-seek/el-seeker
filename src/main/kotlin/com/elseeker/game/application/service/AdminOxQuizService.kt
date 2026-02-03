package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.output.jpa.OxQuestionRepository
import com.elseeker.game.adapter.output.jpa.OxStageRepository
import com.elseeker.game.domain.model.OxQuestion
import com.elseeker.game.domain.model.OxStage
import com.elseeker.game.domain.vo.QuizDifficulty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminOxQuizService(
    private val stageRepository: OxStageRepository,
    private val questionRepository: OxQuestionRepository,
) {
    fun findStages(pageable: Pageable): Page<OxStage> = stageRepository.findAll(pageable)

    fun findStageById(id: Long): OxStage =
        stageRepository.findByIdOrNull(id) ?: throwError(ErrorType.OX_QUIZ_STAGE_NOT_FOUND, "id=$id")

    fun findQuestionsByStage(stageId: Long, pageable: Pageable): Page<OxQuestion> =
        questionRepository.findByStageId(stageId, pageable)

    fun findQuestionById(id: Long): OxQuestion =
        questionRepository.findWithStageById(id) ?: throwError(ErrorType.OX_QUIZ_QUESTION_NOT_FOUND, "id=$id")

    fun getQuestionCountsByStage(): Map<Int, Int> =
        questionRepository.countByStageNumberGroup()
            .associate { it.stageNumber to it.totalQuestions.toInt() }

    @Transactional
    fun createStage(stageNumber: Int, bookName: String): OxStage {
        validateStageNumber(stageNumber)
        if (stageRepository.existsByStageNumber(stageNumber)) {
            throwError(ErrorType.INVALID_PARAMETER, "stageNumber=$stageNumber")
        }
        return stageRepository.save(
            OxStage(
                stageNumber = stageNumber,
                bookName = bookName,
            )
        )
    }

    @Transactional
    fun updateStage(id: Long, stageNumber: Int, bookName: String): OxStage {
        validateStageNumber(stageNumber)
        val stage = findStageById(id)
        if (stage.stageNumber != stageNumber && stageRepository.existsByStageNumber(stageNumber)) {
            throwError(ErrorType.INVALID_PARAMETER, "stageNumber=$stageNumber")
        }
        stageRepository.updateStage(id, stageNumber, bookName)
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
        correctAnswer: Boolean,
        difficulty: QuizDifficulty,
        orderIndex: Int
    ): OxQuestion {
        val stage = findStageById(stageId)
        if (questionRepository.existsByStageIdAndOrderIndex(stageId, orderIndex)) {
            throwError(ErrorType.INVALID_PARAMETER, "orderIndex=$orderIndex")
        }
        return questionRepository.save(
            OxQuestion(
                stage = stage,
                questionText = questionText,
                correctAnswer = correctAnswer,
                difficulty = difficulty,
                orderIndex = orderIndex,
            )
        )
    }

    @Transactional
    fun updateQuestion(
        id: Long,
        questionText: String,
        correctAnswer: Boolean,
        difficulty: QuizDifficulty,
        orderIndex: Int
    ): OxQuestion {
        val existing = findQuestionById(id)
        val stageId = existing.stage.id ?: throwError(ErrorType.OX_QUIZ_STAGE_NOT_FOUND, "questionId=$id")
        if (questionRepository.existsByStageIdAndOrderIndexExcludingId(stageId, orderIndex, id)) {
            throwError(ErrorType.INVALID_PARAMETER, "orderIndex=$orderIndex")
        }
        val updated = OxQuestion(
            id = existing.id,
            stage = existing.stage,
            questionText = questionText,
            correctAnswer = correctAnswer,
            difficulty = difficulty,
            orderIndex = orderIndex,
        )
        return questionRepository.save(updated)
    }

    @Transactional
    fun deleteQuestion(id: Long) {
        val question = findQuestionById(id)
        questionRepository.delete(question)
    }

    private fun validateStageNumber(stageNumber: Int) {
        if (!OxStage.isValidStageNumber(stageNumber)) {
            throwError(
                ErrorType.INVALID_PARAMETER,
                "stageNumber must be between ${OxStage.MIN_STAGE} and ${OxStage.MAX_STAGE}"
            )
        }
    }
}
