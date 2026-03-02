package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.WordPuzzle
import com.elseeker.game.domain.vo.PuzzleStatus
import com.elseeker.game.domain.vo.QuizDifficulty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface WordPuzzleRepository : JpaRepository<WordPuzzle, Long> {

    @Query(
        """
        SELECT wp FROM WordPuzzle wp
        WHERE wp.puzzleStatusCode = :status
        AND (:themeCode IS NULL OR wp.themeCode = :themeCode)
        AND (:difficultyCode IS NULL OR wp.difficultyCode = :difficultyCode)
        ORDER BY wp.id ASC
        """
    )
    fun findPublishedPuzzles(
        @Param("status") status: PuzzleStatus = PuzzleStatus.PUBLISHED,
        @Param("themeCode") themeCode: String?,
        @Param("difficultyCode") difficultyCode: QuizDifficulty?,
        pageable: Pageable
    ): Page<WordPuzzle>

    @Query(
        """
        SELECT wp FROM WordPuzzle wp
        LEFT JOIN FETCH wp.entries
        WHERE wp.id = :id
        """
    )
    fun findByIdWithEntries(@Param("id") id: Long): WordPuzzle?
}
