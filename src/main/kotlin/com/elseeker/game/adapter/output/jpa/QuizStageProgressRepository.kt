package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizStageProgress
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuizStageProgressRepository : JpaRepository<QuizStageProgress, Long> {
    fun deleteAllByMember(member: Member)
    fun findByMemberAndStageNumber(member: Member, stageNumber: Int): QuizStageProgress?
    fun existsByMemberAndStageNumber(member: Member, stageNumber: Int): Boolean
    fun findAllByMember(member: Member): List<QuizStageProgress>

    @Query(
        value = """
        SELECT *
        FROM quiz_stage_progress
        WHERE member_id = :memberId
        """,
        nativeQuery = true
    )
    fun findAllByMemberId(@Param("memberId") memberId: Long): List<QuizStageProgress>

    @Query(
        value = """
        SELECT *
        FROM quiz_stage_progress
        WHERE member_id = :memberId
          AND stage_number = :stageNumber
        """,
        nativeQuery = true
    )
    fun findByMemberIdAndStageNumber(
        @Param("memberId") memberId: Long,
        @Param("stageNumber") stageNumber: Int
    ): QuizStageProgress?
}
