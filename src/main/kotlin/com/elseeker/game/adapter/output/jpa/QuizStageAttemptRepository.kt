package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizStageAttempt
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository

interface QuizStageAttemptRepository : JpaRepository<QuizStageAttempt, Long> {
    fun findAllByMember(member: Member): List<QuizStageAttempt>
    fun findTopByMemberAndStageNumberAndModeAndCompletedAtIsNullOrderByStartedAtDesc(
        member: Member,
        stageNumber: Int,
        mode: com.elseeker.game.domain.vo.QuizStageAttemptMode
    ): QuizStageAttempt?
}
