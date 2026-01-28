package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleOxStageAttempt
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BibleOxStageAttemptRepository : JpaRepository<BibleOxStageAttempt, Long> {

    @Query(
        """
        SELECT a FROM BibleOxStageAttempt a
        WHERE a.member = :member
        AND a.stageNumber = :stageNumber
        AND a.completedAt IS NULL
        ORDER BY a.startedAt DESC
        """
    )
    fun findInProgressAttempt(
        @Param("member") member: Member,
        @Param("stageNumber") stageNumber: Int
    ): BibleOxStageAttempt?

    @Query(
        """
        SELECT a FROM BibleOxStageAttempt a
        LEFT JOIN FETCH a._questionAttempts
        WHERE a.member = :member
        AND a.stageNumber = :stageNumber
        AND a.completedAt IS NULL
        ORDER BY a.startedAt DESC
        """
    )
    fun findInProgressAttemptWithQuestions(
        @Param("member") member: Member,
        @Param("stageNumber") stageNumber: Int
    ): BibleOxStageAttempt?

    @Query(
        """
        SELECT a FROM BibleOxStageAttempt a
        WHERE a.member = :member
        AND a.stageNumber = :stageNumber
        ORDER BY a.completedAt DESC NULLS FIRST
        """
    )
    fun findByMemberAndStageNumber(
        @Param("member") member: Member,
        @Param("stageNumber") stageNumber: Int
    ): List<BibleOxStageAttempt>

    @Query(
        """
        SELECT a FROM BibleOxStageAttempt a
        WHERE a.member = :member
        AND a.completedAt IS NOT NULL
        ORDER BY a.stageNumber ASC, a.completedAt DESC
        """
    )
    fun findCompletedAttemptsByMember(@Param("member") member: Member): List<BibleOxStageAttempt>

    @Query(
        """
        SELECT DISTINCT a.stageNumber FROM BibleOxStageAttempt a
        WHERE a.member = :member
        AND a.completedAt IS NOT NULL
        """
    )
    fun findCompletedStageNumbers(@Param("member") member: Member): Set<Int>

    @Query(
        """
        SELECT MAX(a.score) FROM BibleOxStageAttempt a
        WHERE a.member = :member
        AND a.stageNumber = :stageNumber
        AND a.completedAt IS NOT NULL
        """
    )
    fun findBestScore(
        @Param("member") member: Member,
        @Param("stageNumber") stageNumber: Int
    ): Int?

    @Modifying
    @Query(
        """
        DELETE FROM BibleOxStageAttempt a
        WHERE a.member.id = :memberId
        """
    )
    fun deleteAllByMemberId(@Param("memberId") memberId: Long)
}
