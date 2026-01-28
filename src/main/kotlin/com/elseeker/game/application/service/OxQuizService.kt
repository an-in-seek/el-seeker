package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.request.OxQuizAnswerRequest
import com.elseeker.game.adapter.input.api.response.*
import com.elseeker.game.adapter.output.jpa.OxMemberQuestionAttemptRepository
import com.elseeker.game.adapter.output.jpa.OxQuestionRepository
import com.elseeker.game.adapter.output.jpa.OxMemberStageAttemptRepository
import com.elseeker.game.adapter.output.jpa.OxStageRepository
import com.elseeker.game.domain.model.OxMemberQuestionAttempt
import com.elseeker.game.domain.model.OxStage
import com.elseeker.game.domain.model.OxMemberStageAttempt
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class OxQuizService(
    private val stageRepository: OxStageRepository,
    private val questionRepository: OxQuestionRepository,
    private val stageAttemptRepository: OxMemberStageAttemptRepository,
    private val questionAttemptRepository: OxMemberQuestionAttemptRepository,
    private val memberRepository: MemberRepository
) {

    @Transactional(readOnly = true)
    fun getStage(stageNumber: Int, memberUid: UUID): OxStageResponse {
        validateStageNumber(stageNumber)
        val stage = getStageOrThrowWithQuestions(stageNumber)
        val questions = stage.questions

        return OxStageResponse(
            stageNumber = stage.stageNumber,
            bookName = stage.bookName,
            totalQuestions = questions.size,
            questions = questions.map { q ->
                OxQuestionResponse(
                    questionId = requireNotNull(q.id) { "Question ID is null" },
                    questionText = q.questionText,
                    orderIndex = q.orderIndex
                )
            }
        )
    }

    @Transactional(readOnly = true)
    fun getStages(memberUid: UUID): OxStageListResponse {
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

            OxStageSummaryResponse(
                stageNumber = stageNumber,
                bookName = stage.bookName,
                totalQuestions = questionCount,
                isCompleted = isCompleted,
                bestScore = bestScore,
                hasInProgress = hasInProgress
            )
        }

        return OxStageListResponse(
            totalStages = stages.size,
            stages = stageSummaries
        )
    }

    @Transactional
    fun startStage(stageNumber: Int, memberUid: UUID): OxStageStartResponse {
        validateStageNumber(stageNumber)
        val member = getMember(memberUid)

        // 진행 중인 attempt가 있으면 재사용
        val existingAttempt = stageAttemptRepository.findInProgressAttemptWithQuestions(member, stageNumber)
        if (existingAttempt != null) {
            return OxStageStartResponse(
                stageAttemptId = requireNotNull(existingAttempt.id) { "Attempt ID is null" },
                stageNumber = existingAttempt.stageNumber,
                startedAt = existingAttempt.startedAt,
                currentScore = existingAttempt.score,
                answeredCount = existingAttempt.questionAttempts.size
            )
        }

        // 새 attempt 생성
        val now = Instant.now()
        val newAttempt = OxMemberStageAttempt(
            member = member,
            stageNumber = stageNumber,
            startedAt = now
        )
        val savedAttempt = stageAttemptRepository.save(newAttempt)

        return OxStageStartResponse(
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
        request: OxQuizAnswerRequest,
        memberUid: UUID
    ): OxAnswerResponse {
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
        val questionAttempt = OxMemberQuestionAttempt(
            stageAttempt = attempt,
            question = question,
            selectedAnswer = request.selectedAnswer,
            isCorrect = isCorrect,
            answeredAt = now
        )
        attempt.addQuestionAttempt(questionAttempt)
        questionAttemptRepository.save(questionAttempt)

        return OxAnswerResponse(
            isCorrect = isCorrect,
            correctAnswer = question.correctAnswer,
            currentScore = attempt.score,
            answeredAt = now
        )
    }

    @Transactional
    fun completeStage(stageNumber: Int, memberUid: UUID): OxCompleteResponse {
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

        return OxCompleteResponse(
            score = attempt.score,
            totalQuestions = totalQuestions,
            accuracyPercent = accuracyPercent,
            completedAt = now
        )
    }

    private fun validateStageNumber(stageNumber: Int) {
        if (!OxStage.isValidStageNumber(stageNumber)) {
            throwError(
                ErrorType.INVALID_PARAMETER,
                "stageNumber must be between ${OxStage.MIN_STAGE} and ${OxStage.MAX_STAGE}"
            )
        }
    }

    private fun getStageOrThrowWithQuestions(stageNumber: Int): OxStage {
        return stageRepository.findByStageNumberWithQuestions(stageNumber)
            ?: throwError(ErrorType.OX_QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
    }

    private fun getMember(memberUid: UUID): Member {
        return memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)
    }
}
