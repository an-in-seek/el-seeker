package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.request.QuizStageAnswerRequest
import com.elseeker.game.adapter.input.api.request.QuizStageCompleteRequest
import com.elseeker.game.adapter.input.api.request.QuizStageStartRequest
import com.elseeker.game.adapter.output.jpa.*
import com.elseeker.game.application.component.QuizAttemptPolicy
import com.elseeker.game.application.component.QuizStageValidator
import com.elseeker.game.application.dto.*
import com.elseeker.game.domain.model.QuizProgress
import com.elseeker.game.domain.model.QuizQuestionStat
import com.elseeker.game.domain.model.QuizStageProgress
import com.elseeker.game.domain.vo.QuizStageAttemptMode
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

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
    fun getStage(stageNumber: Int, memberUid: UUID): QuizStageDetailResult {
        val member = getMember(memberUid)
        val memberId = requireNotNull(member.id) { "member id is null" }
        val detailRows = quizStageRepository.findStageDetailRows(stageNumber)
        if (detailRows.isEmpty()) throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")
        val firstRow = detailRows.first()
        val questions = LinkedHashMap<Long, MutableQuestion>()
        detailRows.forEach { row ->
            val questionId = row.questionId ?: return@forEach
            val question = questions.getOrPut(questionId) {
                MutableQuestion(
                    id = questionId,
                    question = row.questionText ?: ""
                )
            }
            val optionIndex = row.optionIndex
            val optionText = row.optionText
            if (optionIndex != null && optionText != null) {
                question.options.add(IndexedOption(optionIndex, optionText))
            }
        }
        val questionSnapshots = questions.values.map { question ->
            val options = question.options.sortedBy { it.index }.map { it.text }
            QuizStageQuestionSnapshot(
                id = question.id,
                question = question.question,
                options = options
            )
        }
        val stageCount = quizStageRepository.count().toInt()
        val progress = getOrCreateProgressByMemberId(memberId, member)
        val progressSnapshot = buildProgressSnapshotByMemberId(progress, stageNumber, stageCount, memberId, member)
        return QuizStageDetailResult(
            stageNumber = firstRow.stageNumber,
            title = firstRow.title,
            questions = questionSnapshots,
            stageCount = stageCount,
            questionCount = questionSnapshots.size,
            progress = progressSnapshot
        )
    }

    @Transactional
    fun getStageSummaries(memberUid: UUID): QuizStageSummaryMapResult {
        val memberId = memberRepository.findIdByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)
        val stageSummaries = quizStageRepository.findStageSummaries()
        val stageCount = stageSummaries.size
        val progress = quizProgressRepository.findByMemberId(memberId)
        val currentStage = progress?.normalizeCurrentStage(stageCount) ?: 1
        val lastCompletedStage = progress?.lastCompletedStage ?: 0
        val stageProgresses = quizStageProgressRepository.findAllByMemberId(memberId).associateBy { it.stageNumber }
        val accuracySummaries = quizQuestionStatRepository.findStageAccuracySummariesByMemberId(memberId).associateBy { it.stageNumber }
        val summaries = stageSummaries.map { summary ->
            val stageNumber = summary.stageNumber
            val isCompleted = progress?.isStageCompleted(stageNumber) ?: false
            val isCurrent = progress?.isStageCurrent(stageNumber, currentStage) ?: (stageNumber == currentStage)
            val isLocked = progress?.isStageLocked(stageNumber, currentStage) ?: (stageNumber > currentStage)
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
            QuizStageSummarySnapshot(
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
        return QuizStageSummaryMapResult(
            currentStage = currentStage,
            lastCompletedStage = lastCompletedStage,
            totalStages = stageCount,
            stages = summaries
        )
    }

    @Transactional
    fun startStage(stageNumber: Int, request: QuizStageStartRequest, memberUid: UUID): QuizStageProgressSnapshot {
        val member = getMember(memberUid)
        val stageCount = quizStageRepository.count().toInt()
        quizStageValidator.requireStageNumberInRange(stageNumber, stageCount)
        val progress = getOrCreateProgress(member)
        val stageProgress = getOrCreateStageProgress(member, stageNumber)
        val stage = getStageOrThrow(stageNumber)
        stageProgress.start(request.mode, request.reviewType)
        quizAttemptPolicy.getOrCreateStageAttempt(member, stage, request.mode, request.startedAt)
        return buildProgressSnapshot(progress, stageNumber, stageCount, member)
    }

    @Transactional
    fun submitAnswer(stageNumber: Int, request: QuizStageAnswerRequest, memberUid: UUID): QuizStageAnswerSnapshot {
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
        return QuizStageAnswerSnapshot(
            isCorrect = isCorrect,
            correctIndex = question.answerIndex,
            currentScore = score,
            currentQuestionIndex = nextIndex
        )
    }

    @Transactional
    fun completeStage(stageNumber: Int, request: QuizStageCompleteRequest, memberUid: UUID): QuizStageCompleteSnapshot {
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
        return QuizStageCompleteSnapshot(
            nextStage = nextStage,
            accuracy = accuracy,
            reviewCount = stageProgress.reviewCount,
            lastScore = stageProgress.lastScore
        )
    }

    @Transactional
    fun resetProgress(memberUid: UUID) {
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

    private fun getOrCreateProgressByMemberId(memberId: Long, memberRef: Member): QuizProgress {
        return quizProgressRepository.findByMemberId(memberId)
            ?: quizProgressRepository.save(QuizProgress(member = memberRef))
    }

    private fun getStageOrThrow(stageNumber: Int) =
        quizStageRepository.findByStageNumber(stageNumber)
            ?: throwError(ErrorType.QUIZ_STAGE_NOT_FOUND, "stageNumber=$stageNumber")

    private fun getMember(memberUid: UUID): Member {
        return memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)
    }

    private fun getOrCreateStageProgress(member: Member, stageNumber: Int): QuizStageProgress {
        return quizStageProgressRepository.findByMemberAndStageNumber(member, stageNumber)
            ?: quizStageProgressRepository.save(QuizStageProgress(member = member, stageNumber = stageNumber))
    }

    private fun buildProgressSnapshot(
        progress: QuizProgress,
        stageNumber: Int,
        stageCount: Int,
        member: Member
    ): QuizStageProgressSnapshot {
        val currentStage = progress.normalizeCurrentStage(stageCount)
        val isCompleted = progress.isStageCompleted(stageNumber)
        val isReviewOnly = isCompleted
        val isBlocked = progress.isStageLocked(stageNumber, currentStage)
        val stageProgress = getOrCreateStageProgress(member, stageNumber)
        val currentQuestionIndex = stageProgress.currentQuestionIndexOrZero()
        val currentScore = stageProgress.currentScoreOrZero()
        val hasInProgress = stageProgress.hasInProgress()
        return QuizStageProgressSnapshot(
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

    private fun buildProgressSnapshotByMemberId(
        progress: QuizProgress,
        stageNumber: Int,
        stageCount: Int,
        memberId: Long,
        memberRef: Member
    ): QuizStageProgressSnapshot {
        val currentStage = progress.normalizeCurrentStage(stageCount)
        val isCompleted = progress.isStageCompleted(stageNumber)
        val isReviewOnly = isCompleted
        val isBlocked = progress.isStageLocked(stageNumber, currentStage)
        val stageProgress = quizStageProgressRepository.findByMemberIdAndStageNumber(memberId, stageNumber)
            ?: quizStageProgressRepository.save(QuizStageProgress(member = memberRef, stageNumber = stageNumber))
        val currentQuestionIndex = stageProgress.currentQuestionIndexOrZero()
        val currentScore = stageProgress.currentScoreOrZero()
        val hasInProgress = stageProgress.hasInProgress()
        return QuizStageProgressSnapshot(
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

private data class IndexedOption(
    val index: Int,
    val text: String
)

private data class MutableQuestion(
    val id: Long,
    val question: String,
    val options: MutableList<IndexedOption> = mutableListOf()
)
