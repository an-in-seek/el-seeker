package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "bible_ox_question_attempt",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_bible_ox_question_attempt_stage_question",
            columnNames = ["stage_attempt_id", "question_id"]
        )
    ],
    indexes = [
        Index(name = "IDX_bible_ox_question_attempt_question", columnList = "question_id")
    ]
)
class BibleOxQuestionAttempt(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_attempt_id", nullable = false)
    val stageAttempt: BibleOxStageAttempt,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: BibleOxQuestion,

    @Column(name = "selected_answer", nullable = false)
    val selectedAnswer: Boolean,

    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean,

    @Column(name = "answered_at", nullable = false)
    val answeredAt: Instant
) : BaseTimeEntity()
