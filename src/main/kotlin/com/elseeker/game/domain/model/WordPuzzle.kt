package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.game.domain.vo.PuzzleStatus
import com.elseeker.game.domain.vo.QuizDifficulty
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "word_puzzle",
    indexes = [
        Index(name = "IDX_word_puzzle_status", columnList = "puzzle_status_code")
    ]
)
class WordPuzzle(

    id: Long? = null,

    @Column(name = "title", nullable = false, length = 200)
    val title: String,

    @Column(name = "theme_code", nullable = false, length = 50)
    val themeCode: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_code", nullable = false, length = 10)
    val difficultyCode: QuizDifficulty,

    @Column(name = "board_width", nullable = false)
    val boardWidth: Int,

    @Column(name = "board_height", nullable = false)
    val boardHeight: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "puzzle_status_code", nullable = false, length = 20)
    var puzzleStatusCode: PuzzleStatus = PuzzleStatus.DRAFT,

    @Column(name = "published_at")
    var publishedAt: Instant? = null,

    @OneToMany(mappedBy = "wordPuzzle", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @OrderBy("clueNumber ASC, directionCode ASC")
    val entries: MutableList<WordPuzzleEntry> = mutableListOf()

) : BaseTimeEntity(id = id)
