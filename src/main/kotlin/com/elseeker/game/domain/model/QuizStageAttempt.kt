package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.game.domain.vo.QuizStageAttemptMode
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "quiz_stage_attempt",
    indexes = [
        Index(name = "IDX_quiz_stage_attempt_member", columnList = "member_id"),
        Index(name = "IDX_quiz_stage_attempt_stage", columnList = "stage_number")
    ]
)
class QuizStageAttempt(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "stage_number", nullable = false)
    val stageNumber: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    val mode: QuizStageAttemptMode,

    @Column(name = "score", nullable = false)
    var score: Int,

    @Column(name = "question_count", nullable = false)
    var questionCount: Int,

    @Column(name = "started_at", nullable = false)
    val startedAt: Instant,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stageAttempt", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("id ASC")
    private val _questionAttempts: MutableList<QuizQuestionAttempt> = mutableListOf()
) : BaseTimeEntity() {

    fun addQuestionAttempt(attempt: QuizQuestionAttempt) {
        _questionAttempts.add(attempt)
    }

    fun complete(score: Int, questionCount: Int, completedAt: Instant?) {
        this.score = score
        this.questionCount = questionCount
        this.completedAt = completedAt ?: Instant.now()
    }
}
