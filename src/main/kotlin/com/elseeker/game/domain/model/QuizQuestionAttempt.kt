package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "quiz_question_attempt",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_quiz_question_attempt_stage_question",
            columnNames = ["stage_attempt_id", "question_id"]
        )
    ],
    indexes = [
        Index(name = "IDX_quiz_question_attempt_stage_attempt", columnList = "stage_attempt_id"),
        Index(name = "IDX_quiz_question_attempt_question", columnList = "question_id")
    ]
)
class QuizQuestionAttempt(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_attempt_id", nullable = false)
    val stageAttempt: QuizStageAttempt,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: QuizQuestion,

    @Column(name = "selected_index", nullable = false)
    val selectedIndex: Int,

    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean,

    @Column(name = "answered_at", nullable = false)
    val answeredAt: Instant
) : BaseTimeEntity()
