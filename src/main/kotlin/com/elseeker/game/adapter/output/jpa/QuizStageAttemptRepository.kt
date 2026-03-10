package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizMemberStageAttempt
import com.elseeker.game.domain.vo.QuizStageAttemptMode
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuizStageAttemptRepository : JpaRepository<QuizMemberStageAttempt, Long> {
    fun findAllByMember(member: Member): List<QuizMemberStageAttempt>
    fun deleteAllByMember(member: Member)
    fun findTopByMemberAndStageNumberAndModeAndCompletedAtIsNullOrderByStartedAtDesc(
        member: Member,
        stageNumber: Int,
        mode: QuizStageAttemptMode
    ): QuizMemberStageAttempt?

    @Query(
        """
        SELECT a.stageNumber AS stageNumber, MAX(a.score) AS bestScore
        FROM QuizMemberStageAttempt a
        WHERE a.member = :member
        AND a.mode = :mode
        AND a.completedAt IS NOT NULL
        GROUP BY a.stageNumber
        """
    )
    fun findBestScoresByMemberAndMode(
        @Param("member") member: Member,
        @Param("mode") mode: QuizStageAttemptMode = QuizStageAttemptMode.RECORD
    ): List<StageBestScoreRow>
}
