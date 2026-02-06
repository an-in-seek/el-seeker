package com.elseeker.game.application.component

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.client.request.QuizStageAnswerRequest
import com.elseeker.game.adapter.output.jpa.QuizQuestionAttemptRepository
import com.elseeker.game.adapter.output.jpa.QuizQuestionStatRepository
import com.elseeker.game.adapter.output.jpa.QuizStageAttemptRepository
import com.elseeker.game.domain.model.*
import com.elseeker.game.domain.vo.QuizStageAttemptMode
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class QuizAttemptPolicy(
    private val quizStageAttemptRepository: QuizStageAttemptRepository,
    private val quizQuestionAttemptRepository: QuizQuestionAttemptRepository,
    private val quizQuestionStatRepository: QuizQuestionStatRepository
) {

    fun getOrCreateStageAttempt(
        member: Member,
        stage: QuizStage,
        mode: QuizStageAttemptMode,
        startedAt: Instant?
    ) {
        val existingAttempt = quizStageAttemptRepository
            .findTopByMemberAndStageNumberAndModeAndCompletedAtIsNullOrderByStartedAtDesc(
                member,
                stage.stageNumber,
                mode
            )
        if (existingAttempt == null) {
            val attempt = QuizMemberStageAttempt(
                member = member,
                stageNumber = stage.stageNumber,
                mode = mode,
                score = 0,
                questionCount = stage.questions.size,
                startedAt = startedAt ?: Instant.now()
            )
            quizStageAttemptRepository.save(attempt)
        }
    }

    fun getOngoingAttempt(
        member: Member,
        stageNumber: Int,
        mode: QuizStageAttemptMode
    ): QuizMemberStageAttempt =
        quizStageAttemptRepository
            .findTopByMemberAndStageNumberAndModeAndCompletedAtIsNullOrderByStartedAtDesc(member, stageNumber, mode)
            ?: throwError(ErrorType.INVALID_PARAMETER, "stageAttempt not started")

    fun recordQuestionAttempt(
        attempt: QuizMemberStageAttempt,
        question: QuizQuestion,
        request: QuizStageAnswerRequest,
        isCorrect: Boolean
    ) {
        val existingAttempt = quizQuestionAttemptRepository.findByStageAttemptAndQuestion(attempt, question)
        if (existingAttempt != null) {
            throwError(ErrorType.INVALID_PARAMETER, "question already answered")
        }
        val questionAttempt = QuizMemberQuestionAttempt(
            stageAttempt = attempt,
            question = question,
            selectedIndex = request.selectedIndex,
            isCorrect = isCorrect,
            answeredAt = request.answeredAt ?: Instant.now()
        )
        attempt.addQuestionAttempt(questionAttempt)
        quizStageAttemptRepository.save(attempt)
    }

    fun completeAttempt(
        attempt: QuizMemberStageAttempt,
        score: Int,
        questionCount: Int,
        completedAt: Instant?
    ) {
        attempt.complete(score = score, questionCount = questionCount, completedAt = completedAt)
        quizStageAttemptRepository.save(attempt)
    }

    fun updateQuestionStatIfRecord(
        mode: QuizStageAttemptMode,
        member: Member,
        question: QuizQuestion,
        isCorrect: Boolean
    ) {
        if (mode == QuizStageAttemptMode.REVIEW) return
        val stat = quizQuestionStatRepository.findByMemberAndQuestionId(member, question.id!!)
            ?: QuizMemberQuestionStat(member = member, question = question)

        stat.attempts += 1
        if (isCorrect) stat.correct += 1
        quizQuestionStatRepository.save(stat)
    }
}
