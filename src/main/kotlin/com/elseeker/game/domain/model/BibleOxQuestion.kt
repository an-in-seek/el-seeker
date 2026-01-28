package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.game.domain.vo.QuizDifficulty
import jakarta.persistence.*

@Entity
@Table(
    name = "bible_ox_question",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_bible_ox_question_stage_order",
            columnNames = ["stage_id", "order_index"]
        )
    ],
    indexes = [
        Index(
            name = "IDX_bible_ox_question_stage",
            columnList = "stage_id"
        )
    ]
)
class BibleOxQuestion(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    val stage: BibleOxStage,

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
