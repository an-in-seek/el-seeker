package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.game.domain.vo.QuizDifficulty
import jakarta.persistence.*

@Entity
@Table(
    name = "ox_quiz_question",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_ox_quiz_question_stage_order",
            columnNames = ["stage_id", "order_index"]
        )
    ],
    indexes = [
        Index(
            name = "IDX_ox_quiz_question_stage",
            columnList = "stage_id"
        )
    ]
)
class OxQuestion(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    val stage: OxStage,

    @Column(name = "question_text", nullable = false, length = 500)
    val questionText: String,

    @Column(name = "correct_answer", nullable = false)
    val correctAnswer: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 10)
    val difficulty: QuizDifficulty = QuizDifficulty.NORMAL,

    @Column(name = "order_index", nullable = false)
    val orderIndex: Int
) : BaseTimeEntity()
