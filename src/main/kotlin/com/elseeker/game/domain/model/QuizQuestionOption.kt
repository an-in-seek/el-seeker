package com.elseeker.game.domain.model

import jakarta.persistence.*

@Entity
@Table(
    name = "quiz_question_option",
    indexes = [
        Index(
            name = "IDX_quiz_option_question",
            columnList = "question_id"
        )
    ]
)
class QuizQuestionOption(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: QuizQuestion,

    @Column(name = "option_text", nullable = false, length = 255)
    val optionText: String,

    @Column(name = "option_index", nullable = false)
    val optionIndex: Int
)
