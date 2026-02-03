package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "ox_quiz_member_stage_attempt",
    indexes = [
        Index(name = "IDX_ox_quiz_member_stage_attempt_member", columnList = "member_id"),
        Index(name = "IDX_ox_quiz_member_stage_attempt_stage", columnList = "stage_number"),
        Index(name = "IDX_ox_quiz_member_stage_attempt_member_stage", columnList = "member_id, stage_number")
    ]
)
class OxMemberStageAttempt(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "stage_number", nullable = false)
    val stageNumber: Int,

    @Column(name = "score", nullable = false)
    var score: Int = 0,

    @Column(name = "started_at", nullable = false)
    val startedAt: Instant,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stageAttempt", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("id ASC")
    private val _questionAttempts: MutableList<OxMemberQuestionAttempt> = mutableListOf()
) : BaseTimeEntity(
    id = id,
) {

    val questionAttempts: List<OxMemberQuestionAttempt>
        get() = _questionAttempts.toList()

    fun addQuestionAttempt(attempt: OxMemberQuestionAttempt) {
        _questionAttempts.add(attempt)
        if (attempt.isCorrect) {
            score++
        }
    }

    fun complete(completedAt: Instant = Instant.now()) {
        this.completedAt = completedAt
    }

    fun isCompleted(): Boolean = completedAt != null

    fun isInProgress(): Boolean = completedAt == null

    fun hasAnsweredQuestion(questionId: Long): Boolean =
        _questionAttempts.any { it.question.id == questionId }
}
