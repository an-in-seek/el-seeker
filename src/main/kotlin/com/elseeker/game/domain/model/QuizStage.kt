package com.elseeker.game.domain.model

import jakarta.persistence.*

@Entity
@Table(
    name = "quiz_stage",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_quiz_stage_number",
            columnNames = ["stage_number"]
        )
    ],
    indexes = [
        Index(
            name = "IDX_quiz_stage_number",
            columnList = "stage_number"
        )
    ]
)
class QuizStage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "stage_number", nullable = false)
    val stageNumber: Int,

    @Column
    val title: String? = null,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stage")
    @OrderBy("id ASC")
    val questions: MutableSet<QuizQuestion> = mutableSetOf()
)
