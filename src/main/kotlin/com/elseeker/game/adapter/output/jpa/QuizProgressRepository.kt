package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizProgress
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository

interface QuizProgressRepository : JpaRepository<QuizProgress, Long> {
    fun findByMember(member: Member): QuizProgress?
}
