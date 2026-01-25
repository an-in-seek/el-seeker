package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.request.QuizStageAnswerRequest
import com.elseeker.game.adapter.input.api.request.QuizStageCompleteRequest
import com.elseeker.game.adapter.input.api.request.QuizStageStartRequest
import com.elseeker.game.adapter.input.api.response.*
import com.elseeker.game.adapter.output.jpa.*
import com.elseeker.game.application.component.QuizAttemptPolicy
import com.elseeker.game.application.component.QuizStageValidator
import com.elseeker.game.application.mapper.toResponse
import com.elseeker.game.domain.model.QuizProgress
import com.elseeker.game.domain.model.QuizQuestionStat
import com.elseeker.game.domain.model.QuizStageProgress
import com.elseeker.game.domain.vo.QuizStageAttemptMode
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BibleQuizService(
    private val quizStageRepository: QuizStageRepository,
    private val quizProgressRepository: QuizProgressRepository,
    private val quizStageProgressRepository: QuizStageProgressRepository,
    private val quizQuestionRepository: QuizQuestionRepository,
    private val quizQuestionStatRepository: QuizQuestionStatRepository,
    private val memberRepository: MemberRepository,
    private val quizAttemptPolicy: QuizAttemptPolicy,
    private val quizStageValidator: QuizStageValidator
) {

    @Transactional
    fun getStage(stageNumber: Int, memberUid: java.util.UUID): QuizStageResponse {
        val member = getMember(memberUid)
        val stage = getStageOrThrow(stageNumber)
        val stageCount = quizStageRepository.count().toInt()
        val progress = getOrCreateProgress(member)
        val progressResponse = buildProgress(progress, stageNumber, stageCount, member)
        return stage.toResponse(stageCount, progressResponse)
    }

    @Transactional
    fun getStageSummaries(memberUid: java.util.UUID): QuizStageMapResponse {
        val member = getMember(memberUid)
        val stageSummaries = quizStageRepository.findStageSummaries()
        val stageCount = stageSummaries.size
        val progress = getOrCreateProgress(member)
        val currentStage = progress.normalizeCurrentStage(stageCount)
        val stageProgresses = quizStageProgressRepository.findAllByMember(member).associateBy { it.stageNumber }
        val accuracySummaries = quizQuestionStatRepository.findStageAccuracySummaries(member).associateBy { it.stageNumber }
        val summaries = stageSummaries.map { summary ->
            val stageNumber = summary.stageNumber
            val isCompleted = progress.isStageCompleted(stageNumber)
            val isCurrent = progress.isStageCurrent(stageNumber, currentStage)
            val isLocked = progress.isStageLocked(stageNumber, currentStage)
            val status = when {
                isCompleted -> "completed"
                isCurrent -> "current"
                else -> "locked"
            }
            val stageProgress = stageProgresses[stageNumber]
            val lastScore = if (isCompleted) stageProgress?.lastScore else null
            val accuracySummary = accuracySummaries[stageNumber]
            val accuracy = accuracySummary?.let { QuizQuestionStat.accuracyPercent(it.attempts, it.correct) }
            val reviewCount = stageProgress?.reviewCount ?: 0
            val hasInProgress = stageProgress?.currentQuestionIndex != null
            QuizStageSummaryResponse(
                stageNumber = stageNumber,
                questionCount = summary.questionCount.toInt(),
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
        return QuizStageMapResponse(
            currentStage = currentStage,
            lastCompletedStage = progress.lastCompletedStage,
            totalStages = stageCount,
            stages = summaries
        )
    }

    @Transactional
    fun startStage(stageNumber: Int, request: QuizStageStartRequest, memberUid: java.util.UUID): QuizStageProgressResponse {
        val member = getMember(memberUid)
        val stageCount = quizStageRepository.count().toInt()
        quizStageValidator.requireStageNumberInRange(stageNumber, stageCount)
        val progress = getOrCreateProgress(member)
        val stageProgress = getOrCreateStageProgress(member, stageNumber)
        val stage = getStageOrThrow(stageNumber)
        stageProgress.start(request.mode, request.reviewType)
        quizAttemptPolicy.getOrCreateStageAttempt(member, stage, request.mode, request.startedAt)
        return buildProgress(progress, stageNumber, stageCount, member)
    }

    @Transactional
    fun submitAnswer(stageNumber: Int, request: QuizStageAnswerRequest, memberUid: java.util.UUID): QuizStageAnswerResponse {
        val member = getMember(memberUid)
        val question = quizQuestionRepository.findById(request.questionId)
            .orElseThrow { throwError(ErrorType.INVALID_PARAMETER, "questionId=${request.questionId}") }
        quizStageValidator.ensureQuestionStageMatch(question.stage.stageNumber, stageNumber, request.questionId)
        val isCorrect = request.selectedIndex == question.answerIndex
        val stageProgress = getOrCreateStageProgress(member, stageNumber)
        val currentIndex = stageProgress.currentQuestionIndex ?: 0
        quizStageValidator.requireQuestionIndexNotAhead(request.questionIndex, currentIndex)
        if (request.questionIndex < currentIndex) {
            throwError(ErrorType.INVALID_PARAMETER, "question already answered")
        }
        val attempt = quizAttemptPolicy.getOngoingAttempt(member, stageNumber, request.mode)
        quizAttemptPolicy.recordQuestionAttempt(attempt, question, request, isCorrect)
        quizAttemptPolicy.updateQuestionStatIfRecord(request.mode, member, question, isCorrect)
        stageProgress.advance(request.questionIndex, isCorrect, request.mode)
        val score = stageProgress.currentScoreOrZero()
        val nextIndex = stageProgress.currentQuestionIndexOrZero()
        return QuizStageAnswerResponse(
            isCorrect = isCorrect,
            correctIndex = question.answerIndex,
            currentScore = score,
            currentQuestionIndex = nextIndex
        )
    }

    @Transactional
    fun completeStage(stageNumber: Int, request: QuizStageCompleteRequest, memberUid: java.util.UUID): QuizStageCompleteResponse {
        val member = getMember(memberUid)
        val stageCount = quizStageRepository.count().toInt()
        quizStageValidator.requireStageNumberInRange(stageNumber, stageCount)
        val stageProgress = getOrCreateStageProgress(member, stageNumber)
        val progress = getOrCreateProgress(member)
        val attempt = quizAttemptPolicy.getOngoingAttempt(member, stageNumber, request.mode)
        if (request.mode == QuizStageAttemptMode.REVIEW) {
            stageProgress.increaseReviewCount()
        } else {
            stageProgress.recordLastScore(request.score)
            progress.completeStage(stageNumber, stageCount)
        }
        quizAttemptPolicy.completeAttempt(
            attempt = attempt,
            score = request.score,
            questionCount = request.questionCount,
            completedAt = request.completedAt
        )
        stageProgress.resetInProgress()
        val accuracy = if (request.mode == QuizStageAttemptMode.REVIEW) null else getStageAccuracy(member, stageNumber)
        val nextStage = progress.currentStageNumber
        return QuizStageCompleteResponse(
            nextStage = nextStage,
            accuracy = accuracy,
            reviewCount = stageProgress.reviewCount,
            lastScore = stageProgress.lastScore
        )
    }

    @Transactional
    fun resetProgress(memberUid: java.util.UUID) {
        val member = getMember(memberUid)
        val progress = getOrCreateProgress(member)
        progress.reset()
        val stageProgresses = quizStageProgressRepository.findAllByMember(member)
        stageProgresses.forEach { stageProgress ->
            stageProgress.resetInProgress()
        }
    }

    private fun getOrCreateProgress(member: Member): QuizProgress {
        return quizProgressRepository.findByMember(member)
            ?: quizProgressRepository.save(QuizProgress(member = member))
    }

    private fun getStageOrThrow(stageNumber: Int) =
        quizStageRepository.findByStageNumber(stageNumber)
            ?: throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")

    private fun getMember(memberUid: java.util.UUID): Member {
        return memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)
    }

    private fun getOrCreateStageProgress(member: Member, stageNumber: Int): QuizStageProgress {
        return quizStageProgressRepository.findByMemberAndStageNumber(member, stageNumber)
            ?: quizStageProgressRepository.save(QuizStageProgress(member = member, stageNumber = stageNumber))
    }

    private fun buildProgress(
        progress: QuizProgress,
        stageNumber: Int,
        stageCount: Int,
        member: Member
    ): QuizStageProgressResponse {
        val currentStage = progress.normalizeCurrentStage(stageCount)
        val isCompleted = progress.isStageCompleted(stageNumber)
        val isReviewOnly = isCompleted
        val isBlocked = progress.isStageLocked(stageNumber, currentStage)
        val stageProgress = getOrCreateStageProgress(member, stageNumber)
        val currentQuestionIndex = stageProgress.currentQuestionIndexOrZero()
        val currentScore = stageProgress.currentScoreOrZero()
        val hasInProgress = stageProgress.hasInProgress()
        return QuizStageProgressResponse(
            stageNumber = stageNumber,
            currentStage = progress.currentStageNumber,
            lastCompletedStage = progress.lastCompletedStage,
            isCompleted = isCompleted,
            isReviewOnly = isReviewOnly,
            isBlocked = isBlocked,
            currentQuestionIndex = currentQuestionIndex,
            currentScore = currentScore,
            currentReviewType = stageProgress.currentReviewType,
            lastScore = stageProgress.lastScore,
            reviewCount = stageProgress.reviewCount,
            hasInProgress = hasInProgress
        )
    }

    private fun getStageAccuracy(member: Member, stageNumber: Int): Int? {
        val stats = quizQuestionStatRepository.findByMemberAndStageNumber(member, stageNumber)
        if (stats.isEmpty()) return null
        val attempts = stats.sumOf { it.attempts }
        val correct = stats.sumOf { it.correct }
        return QuizQuestionStat.accuracyPercent(attempts.toLong(), correct.toLong())
    }

}
