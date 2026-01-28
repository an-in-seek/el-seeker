package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizMemberProgress
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuizProgressRepository : JpaRepository<QuizMemberProgress, Long> {
    fun deleteAllByMember(member: Member)
    fun findByMember(member: Member): QuizMemberProgress?
    fun existsByMember(member: Member): Boolean

    @Query(
        value = """
        SELECT *
        FROM quiz_member_progress
        WHERE member_id = :memberId
        """,
        nativeQuery = true
    )
    fun findByMemberId(@Param("memberId") memberId: Long): QuizMemberProgress?
}
