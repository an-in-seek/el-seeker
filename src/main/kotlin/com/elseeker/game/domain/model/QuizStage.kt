package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "quiz_stage",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_quiz_stage_number",
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

    id: Long? = null,

    @Column(name = "stage_number", nullable = false)
    val stageNumber: Int,

    @Column
    val title: String? = null,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stage", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("id ASC")
    private val _questions: MutableList<QuizQuestion> = mutableListOf()
) : BaseEntity(
    id = id,
) {

    val questions: List<QuizQuestion>
        get() = _questions.toList()

    fun addQuestion(question: QuizQuestion) {
        _questions.add(question)
    }
}
