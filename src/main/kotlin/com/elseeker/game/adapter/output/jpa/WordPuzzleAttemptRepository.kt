package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.WordPuzzleAttempt
import com.elseeker.game.domain.vo.AttemptStatus
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface WordPuzzleAttemptRepository : JpaRepository<WordPuzzleAttempt, Long> {

    @Query(
        """
        SELECT a FROM WordPuzzleAttempt a
        WHERE a.member = :member
        AND a.wordPuzzle.id = :puzzleId
        AND a.attemptStatusCode = :status
        """
    )
    fun findByMemberAndPuzzleIdAndStatus(
        @Param("member") member: Member,
        @Param("puzzleId") puzzleId: Long,
        @Param("status") status: AttemptStatus = AttemptStatus.IN_PROGRESS
    ): WordPuzzleAttempt?

    @Query(
        """
        SELECT a FROM WordPuzzleAttempt a
        WHERE a.member = :member
        AND a.attemptStatusCode = :status
        """
    )
    fun findAllByMemberAndStatus(
        @Param("member") member: Member,
        @Param("status") status: AttemptStatus = AttemptStatus.IN_PROGRESS
    ): List<WordPuzzleAttempt>

    @Query(
        """
        SELECT a FROM WordPuzzleAttempt a
        JOIN FETCH a.wordPuzzle
        WHERE a.id = :id
        AND a.member = :member
        """
    )
    fun findByIdAndMemberWithPuzzle(
        @Param("id") id: Long,
        @Param("member") member: Member
    ): WordPuzzleAttempt?

    @Modifying
    @Query("DELETE FROM WordPuzzleAttempt a WHERE a.member.id = :memberId")
    fun deleteAllByMemberId(@Param("memberId") memberId: Long)
}
