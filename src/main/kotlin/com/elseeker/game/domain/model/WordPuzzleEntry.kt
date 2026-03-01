package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseEntity
import com.elseeker.game.domain.vo.ClueType
import com.elseeker.game.domain.vo.PuzzleDirection
import com.elseeker.study.domain.model.Dictionary
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "word_puzzle_entry",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_word_puzzle_entry_clue",
            columnNames = ["word_puzzle_id", "clue_number", "direction_code"]
        )
    ],
    indexes = [
        Index(name = "IDX_word_puzzle_entry_puzzle", columnList = "word_puzzle_id")
    ]
)
class WordPuzzleEntry(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_puzzle_id", nullable = false)
    val wordPuzzle: WordPuzzle,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictionary_id", nullable = false)
    val dictionary: Dictionary,

    @Column(name = "answer_text", nullable = false, length = 100)
    val answerText: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "direction_code", nullable = false, length = 10)
    val directionCode: PuzzleDirection,

    @Column(name = "start_row", nullable = false)
    val startRow: Int,

    @Column(name = "start_col", nullable = false)
    val startCol: Int,

    @Column(name = "clue_number", nullable = false)
    val clueNumber: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "clue_type_code", nullable = false, length = 20)
    val clueTypeCode: ClueType,

    @Column(name = "clue_text", nullable = false, columnDefinition = "TEXT")
    val clueText: String,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()

) : BaseEntity(id = id) {

    val length: Int
        get() = answerText.length
}
