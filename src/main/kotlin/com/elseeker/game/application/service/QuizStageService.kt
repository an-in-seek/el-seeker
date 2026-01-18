package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.request.QuizStageAnswerRequest
import com.elseeker.game.adapter.input.api.request.QuizStageCompleteRequest
import com.elseeker.game.adapter.input.api.request.QuizStageStartRequest
import com.elseeker.game.adapter.input.api.response.QuizStageAnswerResponse
import com.elseeker.game.adapter.input.api.response.QuizStageCompleteResponse
import com.elseeker.game.adapter.input.api.response.QuizStageContextResponse
import com.elseeker.game.adapter.input.api.response.QuizStageMapResponse
import com.elseeker.game.adapter.input.api.response.QuizStageResponse
import com.elseeker.game.adapter.input.api.response.QuizStageSummaryResponse
import com.elseeker.game.adapter.output.jpa.*
import com.elseeker.game.application.mapper.toResponse
import com.elseeker.game.domain.model.QuizProgress
import com.elseeker.game.domain.model.QuizQuestionStat
import com.elseeker.game.domain.model.QuizStageProgress
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Service
class QuizStageService(
    private val quizStageRepository: QuizStageRepository,
    private val quizProgressRepository: QuizProgressRepository,
    private val quizStageProgressRepository: QuizStageProgressRepository,
    private val quizQuestionRepository: QuizQuestionRepository,
    private val quizQuestionStatRepository: QuizQuestionStatRepository,
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun getStage(stageNumber: Int, memberUid: java.util.UUID): QuizStageResponse {
        val member = getMember(memberUid)
        if (stageNumber < 1) throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
        val stage = quizStageRepository.findByStageNumber(stageNumber)
            ?: throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
        val stageCount = quizStageRepository.count().toInt()
        val progress = getOrCreateProgress(member)
        val context = buildContext(progress, stageNumber, stageCount, member)
        return stage.toResponse(stageCount, context)
    }

    @Transactional
    fun getStageSummaries(memberUid: java.util.UUID): QuizStageMapResponse {
        val member = getMember(memberUid)
        val stageSummaries = quizStageRepository.findStageSummaries()
        val stageCount = stageSummaries.size
        val progress = getOrCreateProgress(member)
        val currentStage = normalizeCurrentStage(progress, stageCount)
        val stageProgresses = quizStageProgressRepository.findAllByMember(member)
            .associateBy { it.stageNumber }
        val accuracySummaries = quizQuestionStatRepository.findStageAccuracySummaries(member)
            .associateBy { it.stageNumber }

        val summaries = stageSummaries.map { summary ->
            val stageNumber = summary.stageNumber
            val status = when {
                stageNumber < currentStage -> "completed"
                stageNumber == currentStage -> "active"
                else -> "locked"
            }
            val stageProgress = stageProgresses[stageNumber]
            val score = if (status == "completed") stageProgress?.lastScore else null
            val accuracySummary = accuracySummaries[stageNumber]
            val accuracy = accuracySummary?.let { calculateAccuracy(it.attempts, it.correct) }
            val reviewCount = stageProgress?.reviewCount ?: 0
            QuizStageSummaryResponse(
                stage = stageNumber,
                questionCount = summary.questionCount.toInt(),
                status = status,
                score = score,
                accuracy = accuracy,
                reviewCount = reviewCount
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
    fun startStage(stageNumber: Int, request: QuizStageStartRequest, memberUid: java.util.UUID): QuizStageContextResponse {
        val member = getMember(memberUid)
        if (stageNumber < 1) throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
        val stageCount = quizStageRepository.count().toInt()
        if (stageCount < stageNumber) throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
        val progress = getOrCreateProgress(member)
        val stageProgress = getOrCreateStageProgress(member, stageNumber)

        if (request.mode == "review") {
            stageProgress.currentReviewType = request.reviewType ?: "full"
        } else {
            stageProgress.currentReviewType = null
            if (stageProgress.currentScore == null) stageProgress.currentScore = 0
        }

        return buildContext(progress, stageNumber, stageCount, member)
    }

    @Transactional
    fun submitAnswer(stageNumber: Int, request: QuizStageAnswerRequest, memberUid: java.util.UUID): QuizStageAnswerResponse {
        val member = getMember(memberUid)
        val question = quizQuestionRepository.findById(request.questionId)
            .orElseThrow { throwError(ErrorType.INVALID_PARAMETER, "questionId=${request.questionId}") }

        if (question.stage.stageNumber != stageNumber) {
            throwError(ErrorType.INVALID_PARAMETER, "stageNumber=$stageNumber", "questionId=${request.questionId}")
        }

        val isCorrect = request.selectedIndex == question.answerIndex
        val stageProgress = getOrCreateStageProgress(member, stageNumber)
        val currentIndex = stageProgress.currentQuestionIndex
        val isDuplicate = currentIndex != null && request.questionIndex < currentIndex

        if (!isDuplicate) {
            if (request.mode != "review") {
                val stat = quizQuestionStatRepository.findByMemberAndQuestionId(member, question.id!!)
                    ?: QuizQuestionStat(member = member, question = question)

                stat.attempts += 1
                if (isCorrect) stat.correct += 1
                quizQuestionStatRepository.save(stat)
            }

            stageProgress.currentQuestionIndex = request.questionIndex + 1

            if (request.mode != "review") {
                val currentScore = stageProgress.currentScore ?: 0
                stageProgress.currentScore = currentScore + if (isCorrect) 1 else 0
            }
        }

        val score = stageProgress.currentScore ?: 0
        return QuizStageAnswerResponse(
            isCorrect = isCorrect,
            correctIndex = question.answerIndex,
            currentScore = score
        )
    }

    @Transactional
    fun completeStage(stageNumber: Int, request: QuizStageCompleteRequest, memberUid: java.util.UUID): QuizStageCompleteResponse {
        val member = getMember(memberUid)
        if (stageNumber < 1) throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
        val stageCount = quizStageRepository.count().toInt()
        if (stageCount < stageNumber) throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")

        val stageProgress = getOrCreateStageProgress(member, stageNumber)
        val progress = getOrCreateProgress(member)

        if (request.mode == "review") {
            stageProgress.reviewCount += 1
        } else {
            stageProgress.lastScore = request.score
            val nextStage = calculateNextStage(stageNumber, stageCount)
            progress.currentStage = nextStage
            progress.lastCompletedStage = max(progress.lastCompletedStage, stageNumber)
        }

        stageProgress.currentQuestionIndex = null
        stageProgress.currentScore = null
        stageProgress.currentReviewType = null

        val accuracy = if (request.mode == "review") null else getStageAccuracy(member, stageNumber)
        val nextStage = calculateNextStage(stageNumber, stageCount)

        return QuizStageCompleteResponse(
            nextStage = nextStage,
            accuracy = accuracy,
            reviewCount = stageProgress.reviewCount
        )
    }

    @Transactional
    fun resetProgress(memberUid: java.util.UUID) {
        val member = getMember(memberUid)
        val progress = getOrCreateProgress(member)
        progress.currentStage = 1
        progress.lastCompletedStage = 0

        val stageProgresses = quizStageProgressRepository.findAllByMember(member)
        stageProgresses.forEach { stageProgress ->
            stageProgress.currentQuestionIndex = null
            stageProgress.currentScore = null
            stageProgress.currentReviewType = null
        }
    }

    private fun getOrCreateProgress(member: Member): QuizProgress {
        return quizProgressRepository.findByMember(member)
            ?: quizProgressRepository.save(QuizProgress(member = member))
    }

    private fun getMember(memberUid: java.util.UUID): Member {
        return memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)
    }

    private fun getOrCreateStageProgress(member: Member, stageNumber: Int): QuizStageProgress {
        return quizStageProgressRepository.findByMemberAndStageNumber(member, stageNumber)
            ?: quizStageProgressRepository.save(QuizStageProgress(member = member, stageNumber = stageNumber))
    }

    private fun buildContext(
        progress: QuizProgress,
        stageNumber: Int,
        stageCount: Int,
        member: Member
    ): QuizStageContextResponse {
        val normalizedCurrentStage = normalizeCurrentStage(progress, stageCount)
        val currentStage = normalizedCurrentStage
        val isCompletedStage = stageNumber <= progress.lastCompletedStage && progress.lastCompletedStage > 0
        val isReviewOnly = isCompletedStage
        val isBlocked = stageNumber > currentStage

        val stageProgress = getOrCreateStageProgress(member, stageNumber)
        val currentQuestionIndex = stageProgress.currentQuestionIndex ?: 0
        val currentScore = stageProgress.currentScore ?: 0
        val hasInProgress = stageProgress.currentQuestionIndex != null

        return QuizStageContextResponse(
            activeStage = stageNumber,
            boundedCurrentStage = currentStage,
            currentStage = progress.currentStage,
            lastCompletedStage = progress.lastCompletedStage,
            isCompletedStage = isCompletedStage,
            isReviewOnly = isReviewOnly,
            isBlocked = isBlocked,
            currentQuestionIndex = currentQuestionIndex,
            currentScore = currentScore,
            reviewType = stageProgress.currentReviewType,
            hasInProgress = hasInProgress
        )
    }

    private fun normalizeCurrentStage(progress: QuizProgress, stageCount: Int): Int {
        val storedCurrentStage = normalizeStage(progress.currentStage, stageCount)
        val lastCompleted = max(0, progress.lastCompletedStage)
        val currentStage = if (lastCompleted >= storedCurrentStage) {
            lastCompleted + 1
        } else {
            storedCurrentStage
        }
        val bounded = if (stageCount > 0) clamp(currentStage, 1, stageCount) else max(currentStage, 1)
        if (progress.currentStage != bounded) {
            progress.currentStage = bounded
        }
        if (progress.lastCompletedStage < 0) {
            progress.lastCompletedStage = 0
        }
        return bounded
    }

    private fun normalizeStage(value: Int?, stageCount: Int): Int {
        val parsed = value ?: 1
        if (parsed < 1) return 1
        return if (stageCount > 0) clamp(parsed, 1, stageCount) else max(parsed, 1)
    }

    private fun clamp(value: Int, min: Int, max: Int): Int = min(max(value, min), max)

    private fun calculateNextStage(currentStage: Int, stageCount: Int): Int {
        return if (stageCount > 0) min(currentStage + 1, stageCount) else currentStage + 1
    }

    private fun getStageAccuracy(member: Member, stageNumber: Int): Int? {
        val stats = quizQuestionStatRepository.findByMemberAndStageNumber(member, stageNumber)
        if (stats.isEmpty()) return null
        val attempts = stats.sumOf { it.attempts }
        val correct = stats.sumOf { it.correct }
        return calculateAccuracy(attempts.toLong(), correct.toLong())
    }

    private fun calculateAccuracy(attempts: Long, correct: Long): Int? {
        if (attempts <= 0) return null
        return ((correct.toDouble() / attempts.toDouble()) * 100).roundToInt()
    }

}
