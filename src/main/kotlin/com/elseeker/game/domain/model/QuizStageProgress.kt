package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.game.domain.vo.QuizStageAttemptMode
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*

@Entity
@Table(
    name = "quiz_stage_progress",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_quiz_stage_progress_member_stage",
            columnNames = ["member_id", "stage_number"]
        )
    ],
    indexes = [
        Index(
            name = "IDX_quiz_stage_progress_member",
            columnList = "member_id"
        ),
        Index(
            name = "IDX_quiz_stage_progress_stage",
            columnList = "stage_number"
        )
    ]
)
class QuizStageProgress(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "stage_number", nullable = false)
    val stageNumber: Int,

    @Column(name = "current_question_index")
    var currentQuestionIndex: Int? = null,

    @Column(name = "current_score")
    var currentScore: Int? = null,

    @Column(name = "last_score")
    var lastScore: Int? = null,

    @Column(name = "review_count", nullable = false)
    var reviewCount: Int = 0,

    @Column(name = "current_review_type", length = 20)
    var currentReviewType: String? = null
) : BaseTimeEntity() {

    fun start(mode: QuizStageAttemptMode, reviewType: String?) {
        if (mode == QuizStageAttemptMode.REVIEW) {
            currentReviewType = reviewType ?: "full"
        } else {
            currentReviewType = null
        }
        if (currentScore == null) {
            currentScore = 0
        }
        if (currentQuestionIndex == null) {
            currentQuestionIndex = 0
        }
    }

    fun advance(questionIndex: Int, isCorrect: Boolean, mode: QuizStageAttemptMode) {
        currentQuestionIndex = questionIndex + 1
        val score = currentScore ?: 0
        currentScore = score + if (isCorrect) 1 else 0
    }

    fun increaseReviewCount() {
        reviewCount += 1
    }

    fun recordLastScore(score: Int) {
        lastScore = score
    }

    fun resetInProgress() {
        currentQuestionIndex = null
        currentScore = null
        currentReviewType = null
    }

    fun currentQuestionIndexOrZero(): Int = currentQuestionIndex ?: 0

    fun currentScoreOrZero(): Int = currentScore ?: 0

    fun hasInProgress(): Boolean = currentQuestionIndex != null
}
