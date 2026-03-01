package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.WordPuzzleEntry
import com.elseeker.game.domain.vo.PuzzleDirection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface WordPuzzleEntryRepository : JpaRepository<WordPuzzleEntry, Long> {

    @Query(
        """
        SELECT e FROM WordPuzzleEntry e
        JOIN FETCH e.dictionary
        WHERE e.wordPuzzle.id = :puzzleId
        ORDER BY e.clueNumber ASC, e.directionCode ASC
        """
    )
    fun findAllByPuzzleIdWithDictionary(@Param("puzzleId") puzzleId: Long): List<WordPuzzleEntry>

    @Query(
        """
        SELECT e FROM WordPuzzleEntry e
        JOIN FETCH e.dictionary
        WHERE e.id = :id
        """
    )
    fun findByIdWithDictionary(@Param("id") id: Long): WordPuzzleEntry?

    @Query(
        value = """
        SELECT e FROM WordPuzzleEntry e
        JOIN FETCH e.dictionary
        WHERE e.wordPuzzle.id = :puzzleId
        ORDER BY e.clueNumber ASC, e.directionCode ASC
        """,
        countQuery = """
        SELECT COUNT(e) FROM WordPuzzleEntry e
        WHERE e.wordPuzzle.id = :puzzleId
        """
    )
    fun findAllByPuzzleIdWithDictionary(
        @Param("puzzleId") puzzleId: Long,
        pageable: Pageable
    ): Page<WordPuzzleEntry>

    fun existsByWordPuzzleIdAndClueNumberAndDirectionCode(
        wordPuzzleId: Long,
        clueNumber: Int,
        directionCode: PuzzleDirection
    ): Boolean
}
