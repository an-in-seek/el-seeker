package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "word_puzzle_attempt_cell",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_word_puzzle_attempt_cell_position",
            columnNames = ["word_puzzle_attempt_id", "row_index", "col_index"]
        )
    ],
    indexes = [
        Index(name = "IDX_word_puzzle_attempt_cell_attempt", columnList = "word_puzzle_attempt_id")
    ]
)
class WordPuzzleAttemptCell(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_puzzle_attempt_id", nullable = false)
    val attempt: WordPuzzleAttempt,

    @Column(name = "row_index", nullable = false)
    val rowIndex: Int,

    @Column(name = "col_index", nullable = false)
    val colIndex: Int,

    @Column(name = "input_letter", length = 5)
    var inputLetter: String? = null,

    @Column(name = "is_revealed", nullable = false)
    var isRevealed: Boolean = false

) : BaseTimeEntity(id = id) {

    fun reveal(letter: String) {
        this.inputLetter = letter
        this.isRevealed = true
    }

    fun updateLetter(letter: String?) {
        if (!isRevealed) {
            this.inputLetter = letter
        }
    }
}
