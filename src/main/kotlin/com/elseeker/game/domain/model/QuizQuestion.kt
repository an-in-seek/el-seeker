package com.elseeker.game.domain.model

import com.elseeker.game.domain.vo.QuizDifficulty
import jakarta.persistence.*

@Entity
@Table(
    name = "quiz_question",
    indexes = [
        Index(
            name = "IDX_quiz_question_stage",
            columnList = "stage_id"
        )
    ]
)
class QuizQuestion(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    val stage: QuizStage,

    @Column(name = "question_text", nullable = false, length = 500)
    val questionText: String,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "question")
    @OrderBy("optionIndex ASC")
    val options: MutableSet<QuizOption> = mutableSetOf(),

    @Column(name = "answer_index", nullable = false)
    val answerIndex: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    val difficulty: QuizDifficulty? = null
)
