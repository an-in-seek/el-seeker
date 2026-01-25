package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizProgress
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuizProgressRepository : JpaRepository<QuizProgress, Long> {
    fun deleteAllByMember(member: Member)
    fun findByMember(member: Member): QuizProgress?
    fun existsByMember(member: Member): Boolean

    @Query(
        value = """
        SELECT *
        FROM quiz_progress
        WHERE member_id = :memberId
        """,
        nativeQuery = true
    )
    fun findByMemberId(@Param("memberId") memberId: Long): QuizProgress?
}
