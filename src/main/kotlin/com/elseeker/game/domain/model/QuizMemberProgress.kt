package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import kotlin.math.max
import kotlin.math.min

@Entity
@Table(
    name = "quiz_member_progress",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_quiz_progress_member",
            columnNames = ["member_id"]
        )
    ]
)
class QuizMemberProgress(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "current_stage", nullable = false)
    var currentStageNumber: Int = 1,

    @Column(name = "last_completed_stage", nullable = false)
    var lastCompletedStage: Int = 0
) : BaseTimeEntity() {

    fun normalizeCurrentStage(stageCount: Int): Int {
        val storedCurrentStage = normalizeStage(currentStageNumber, stageCount)
        val lastCompleted = max(0, lastCompletedStage)
        val currentStage = if (lastCompleted >= storedCurrentStage) {
            lastCompleted + 1
        } else {
            storedCurrentStage
        }
        val bounded = if (stageCount > 0) clamp(currentStage, 1, stageCount) else max(currentStage, 1)
        if (currentStageNumber != bounded) {
            currentStageNumber = bounded
        }
        if (lastCompletedStage < 0) {
            lastCompletedStage = 0
        }
        return bounded
    }

    fun completeStage(stageNumber: Int, stageCount: Int): Int {
        lastCompletedStage = max(lastCompletedStage, stageNumber)
        currentStageNumber = calculateNextStage(stageNumber, stageCount)
        return currentStageNumber
    }

    fun reset() {
        currentStageNumber = 1
        lastCompletedStage = 0
    }

    fun isStageCompleted(stageNumber: Int): Boolean =
        stageNumber <= lastCompletedStage && lastCompletedStage > 0

    fun isStageCurrent(stageNumber: Int, currentStage: Int): Boolean =
        stageNumber == currentStage

    fun isStageLocked(stageNumber: Int, currentStage: Int): Boolean =
        stageNumber > currentStage

    private fun normalizeStage(value: Int?, stageCount: Int): Int {
        val parsed = value ?: 1
        if (parsed < 1) return 1
        return if (stageCount > 0) clamp(parsed, 1, stageCount) else max(parsed, 1)
    }

    private fun calculateNextStage(currentStage: Int, stageCount: Int): Int {
        return if (stageCount > 0) min(currentStage + 1, stageCount) else currentStage + 1
    }

    private fun clamp(value: Int, min: Int, max: Int): Int = min(max(value, min), max)
}
