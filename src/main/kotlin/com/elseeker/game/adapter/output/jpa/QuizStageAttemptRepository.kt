package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizMemberStageAttempt
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository

interface QuizStageAttemptRepository : JpaRepository<QuizMemberStageAttempt, Long> {
    fun findAllByMember(member: Member): List<QuizMemberStageAttempt>
    fun deleteAllByMember(member: Member)
    fun findTopByMemberAndStageNumberAndModeAndCompletedAtIsNullOrderByStartedAtDesc(
        member: Member,
        stageNumber: Int,
        mode: com.elseeker.game.domain.vo.QuizStageAttemptMode
    ): QuizMemberStageAttempt?
}
