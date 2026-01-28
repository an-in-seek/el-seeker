package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "bible_ox_stage",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_bible_ox_stage_number",
            columnNames = ["stage_number"]
        )
    ]
)
class BibleOxStage(

    @Column(name = "stage_number", nullable = false)
    val stageNumber: Int,

    @Column(name = "book_name", nullable = false, length = 50)
    val bookName: String,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stage", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private val _questions: MutableList<BibleOxQuestion> = mutableListOf()
) : BaseTimeEntity() {

    // 외부에는 불변 리스트로 노출
    val questions: List<BibleOxQuestion>
        get() = _questions.toList()

    // 연관관계 편의 메서드 (Question 생성 시 Stage가 이미 주입되었다고 가정)
    fun addQuestion(question: BibleOxQuestion) {
        _questions.add(question)
    }

    companion object {
        const val MIN_STAGE = 1
        const val MAX_STAGE = 66
        const val QUESTIONS_PER_STAGE = 10

        fun isValidStageNumber(stageNumber: Int): Boolean =
            stageNumber in MIN_STAGE..MAX_STAGE
    }
}
