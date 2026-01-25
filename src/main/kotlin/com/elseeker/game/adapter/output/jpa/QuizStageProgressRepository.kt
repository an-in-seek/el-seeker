package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizStageProgress
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository

interface QuizStageProgressRepository : JpaRepository<QuizStageProgress, Long> {
    fun deleteAllByMember(member: Member)
    fun findByMemberAndStageNumber(member: Member, stageNumber: Int): QuizStageProgress?
    fun existsByMemberAndStageNumber(member: Member, stageNumber: Int): Boolean
    fun findAllByMember(member: Member): List<QuizStageProgress>
}
