package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "ox_quiz_member_question_attempt",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_ox_quiz_member_question_attempt_stage_question",
            columnNames = ["stage_attempt_id", "question_id"]
        )
    ],
    indexes = [
        Index(name = "IDX_ox_quiz_member_question_attempt_question", columnList = "question_id")
    ]
)
class OxMemberQuestionAttempt(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_attempt_id", nullable = false)
    val stageAttempt: OxMemberStageAttempt,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: OxQuestion,

    @Column(name = "selected_answer", nullable = false)
    val selectedAnswer: Boolean,

    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean,

    @Column(name = "answered_at", nullable = false)
    val answeredAt: Instant
) : BaseTimeEntity()
