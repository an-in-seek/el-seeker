package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.OxMemberStageAttempt
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OxMemberStageAttemptRepository : JpaRepository<OxMemberStageAttempt, Long> {

    @Query(
        """
        SELECT a FROM OxMemberStageAttempt a
        WHERE a.member = :member
        AND a.stageNumber = :stageNumber
        AND a.completedAt IS NULL
        ORDER BY a.startedAt DESC
        """
    )
    fun findInProgressAttempt(
        @Param("member") member: Member,
        @Param("stageNumber") stageNumber: Int
    ): OxMemberStageAttempt?

    @Query(
        """
        SELECT a FROM OxMemberStageAttempt a
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
    ): OxMemberStageAttempt?

    @Query(
        """
        SELECT a FROM OxMemberStageAttempt a
        WHERE a.member = :member
        AND a.stageNumber = :stageNumber
        ORDER BY a.completedAt DESC NULLS FIRST
        """
    )
    fun findByMemberAndStageNumber(
        @Param("member") member: Member,
        @Param("stageNumber") stageNumber: Int
    ): List<OxMemberStageAttempt>

    @Query(
        """
        SELECT a FROM OxMemberStageAttempt a
        WHERE a.member = :member
        AND a.completedAt IS NOT NULL
        ORDER BY a.stageNumber ASC, a.completedAt DESC
        """
    )
    fun findCompletedAttemptsByMember(@Param("member") member: Member): List<OxMemberStageAttempt>

    @Query(
        """
        SELECT a.stageNumber AS stageNumber, MAX(a.score) AS bestScore
        FROM OxMemberStageAttempt a
        WHERE a.member = :member
        AND a.completedAt IS NOT NULL
        GROUP BY a.stageNumber
        """
    )
    fun findBestScoresByMember(@Param("member") member: Member): List<StageBestScoreRow>

    @Query(
        """
        SELECT a.stageNumber FROM OxMemberStageAttempt a
        WHERE a.member = :member
        AND a.completedAt IS NULL
        """
    )
    fun findInProgressStageNumbers(@Param("member") member: Member): List<Int>

    @Modifying
    @Query(
        """
        DELETE FROM OxMemberStageAttempt a
        WHERE a.member.id = :memberId
        """
    )
    fun deleteAllByMemberId(@Param("memberId") memberId: Long)
}

interface StageBestScoreRow {
    val stageNumber: Int
    val bestScore: Int?
}
