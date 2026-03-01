package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseEntity
import com.elseeker.game.domain.vo.HintType
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "word_puzzle_hint_usage",
    indexes = [
        Index(name = "IDX_word_puzzle_hint_usage_attempt", columnList = "word_puzzle_attempt_id")
    ]
)
class WordPuzzleHintUsage(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_puzzle_attempt_id", nullable = false)
    val attempt: WordPuzzleAttempt,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_puzzle_entry_id", nullable = false)
    val entry: WordPuzzleEntry,

    @Column(name = "row_index")
    val rowIndex: Int? = null,

    @Column(name = "col_index")
    val colIndex: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "hint_type_code", nullable = false, length = 20)
    val hintTypeCode: HintType,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()

) : BaseEntity(id = id)
