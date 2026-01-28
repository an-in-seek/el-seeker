package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.request.BibleOxQuizAnswerRequest
import com.elseeker.game.adapter.input.api.response.*
import com.elseeker.game.adapter.output.jpa.BibleOxQuestionAttemptRepository
import com.elseeker.game.adapter.output.jpa.BibleOxQuestionRepository
import com.elseeker.game.adapter.output.jpa.BibleOxStageAttemptRepository
import com.elseeker.game.adapter.output.jpa.BibleOxStageRepository
import com.elseeker.game.domain.model.BibleOxQuestionAttempt
import com.elseeker.game.domain.model.BibleOxStage
import com.elseeker.game.domain.model.BibleOxStageAttempt
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class BibleOxQuizService(
    private val stageRepository: BibleOxStageRepository,
    private val questionRepository: BibleOxQuestionRepository,
    private val stageAttemptRepository: BibleOxStageAttemptRepository,
    private val questionAttemptRepository: BibleOxQuestionAttemptRepository,
    private val memberRepository: MemberRepository
) {

    @Transactional(readOnly = true)
    fun getStage(stageNumber: Int, memberUid: UUID): BibleOxStageResponse {
        validateStageNumber(stageNumber)
        val stage = getStageOrThrowWithQuestions(stageNumber)
        val questions = stage.questions

        return BibleOxStageResponse(
            stageNumber = stage.stageNumber,
            bookName = stage.bookName,
            totalQuestions = questions.size,
            questions = questions.map { q ->
                BibleOxQuestionResponse(
                    questionId = requireNotNull(q.id) { "Question ID is null" },
                    questionText = q.questionText,
                    orderIndex = q.orderIndex
                )
            }
        )
    }

    @Transactional(readOnly = true)
    fun getStages(memberUid: UUID): BibleOxStageListResponse {
        val member = getMember(memberUid)
        val stages = stageRepository.findAllOrderByStageNumber()
        val bestScoreMap = stageAttemptRepository.findBestScoresByMember(member)
            .associate { it.stageNumber to it.bestScore }
        val inProgressStageNumbers = stageAttemptRepository.findInProgressStageNumbers(member).toSet()
        val questionCountMap = questionRepository.countByStageNumberGroup()
            .associate { it.stageNumber to it.totalQuestions.toInt() }

        val stageSummaries = stages.map { stage ->
            val stageNumber = stage.stageNumber
            val bestScore = bestScoreMap[stageNumber]
            val isCompleted = bestScore != null
            val hasInProgress = stageNumber in inProgressStageNumbers
            val questionCount = questionCountMap[stageNumber] ?: 0

            BibleOxStageSummaryResponse(
                stageNumber = stageNumber,
                bookName = stage.bookName,
                totalQuestions = questionCount,
                isCompleted = isCompleted,
                bestScore = bestScore,
                hasInProgress = hasInProgress
            )
        }

        return BibleOxStageListResponse(
            totalStages = stages.size,
            stages = stageSummaries
        )
    }

    @Transactional
    fun startStage(stageNumber: Int, memberUid: UUID): BibleOxStageStartResponse {
        validateStageNumber(stageNumber)
        val member = getMember(memberUid)

        // 진행 중인 attempt가 있으면 재사용
        val existingAttempt = stageAttemptRepository.findInProgressAttemptWithQuestions(member, stageNumber)
        if (existingAttempt != null) {
            return BibleOxStageStartResponse(
                stageAttemptId = requireNotNull(existingAttempt.id) { "Attempt ID is null" },
                stageNumber = existingAttempt.stageNumber,
                startedAt = existingAttempt.startedAt,
                currentScore = existingAttempt.score,
                answeredCount = existingAttempt.questionAttempts.size
            )
        }

        // 새 attempt 생성
        val now = Instant.now()
        val newAttempt = BibleOxStageAttempt(
            member = member,
            stageNumber = stageNumber,
            startedAt = now
        )
        val savedAttempt = stageAttemptRepository.save(newAttempt)

        return BibleOxStageStartResponse(
            stageAttemptId = requireNotNull(savedAttempt.id) { "Attempt ID is null" },
            stageNumber = savedAttempt.stageNumber,
            startedAt = savedAttempt.startedAt,
            currentScore = 0,
            answeredCount = 0
        )
    }

    @Transactional
    fun submitAnswer(
        stageNumber: Int,
        questionId: Long,
        request: BibleOxQuizAnswerRequest,
        memberUid: UUID
    ): BibleOxAnswerResponse {
        validateStageNumber(stageNumber)
        val member = getMember(memberUid)

        // 진행 중인 attempt 확인
        val attempt = stageAttemptRepository.findInProgressAttemptWithQuestions(member, stageNumber)
            ?: throwError(ErrorType.OX_QUIZ_ATTEMPT_NOT_FOUND, "stageNumber=$stageNumber")

        // 문제 확인
        val question = questionRepository.findByIdWithStage(questionId)
            ?: throwError(ErrorType.OX_QUIZ_QUESTION_NOT_FOUND, "questionId=$questionId")

        // 문제가 해당 스테이지에 속하는지 확인
        if (question.stage.stageNumber != stageNumber) {
            throwError(ErrorType.OX_QUIZ_QUESTION_MISMATCH, "questionId=$questionId, stageNumber=$stageNumber")
        }

        // 이미 답변한 문제인지 확인
        if (attempt.hasAnsweredQuestion(questionId)) {
            throwError(ErrorType.OX_QUIZ_ALREADY_ANSWERED, "questionId=$questionId")
        }

        // 정답 확인
        val isCorrect = request.selectedAnswer == question.correctAnswer
        val now = Instant.now()

        // QuestionAttempt 저장
        val questionAttempt = BibleOxQuestionAttempt(
            stageAttempt = attempt,
            question = question,
            selectedAnswer = request.selectedAnswer,
            isCorrect = isCorrect,
            answeredAt = now
        )
        attempt.addQuestionAttempt(questionAttempt)
        questionAttemptRepository.save(questionAttempt)

        return BibleOxAnswerResponse(
            isCorrect = isCorrect,
            correctAnswer = question.correctAnswer,
            currentScore = attempt.score,
            answeredAt = now
        )
    }

    @Transactional
    fun completeStage(stageNumber: Int, memberUid: UUID): BibleOxCompleteResponse {
        validateStageNumber(stageNumber)
        val member = getMember(memberUid)

        // 진행 중인 attempt 확인
        val attempt = stageAttemptRepository.findInProgressAttemptWithQuestions(member, stageNumber)
            ?: throwError(ErrorType.OX_QUIZ_ATTEMPT_NOT_FOUND, "stageNumber=$stageNumber")

        val now = Instant.now()
        attempt.complete(now)

        val totalQuestions = questionRepository.countByStageNumber(stageNumber).toInt()
        val accuracyPercent = if (totalQuestions > 0) {
            (attempt.score * 100) / totalQuestions
        } else 0

        return BibleOxCompleteResponse(
            score = attempt.score,
            totalQuestions = totalQuestions,
            accuracyPercent = accuracyPercent,
            completedAt = now
        )
    }

    private fun validateStageNumber(stageNumber: Int) {
        if (!BibleOxStage.isValidStageNumber(stageNumber)) {
            throwError(
                ErrorType.INVALID_PARAMETER,
                "stageNumber must be between ${BibleOxStage.MIN_STAGE} and ${BibleOxStage.MAX_STAGE}"
            )
        }
    }

    private fun getStageOrThrowWithQuestions(stageNumber: Int): BibleOxStage {
        return stageRepository.findByStageNumberWithQuestions(stageNumber)
            ?: throwError(ErrorType.OX_QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
    }

    private fun getMember(memberUid: UUID): Member {
        return memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)
    }
}
