package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizQuestionStat
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface QuizQuestionStatRepository : JpaRepository<QuizQuestionStat, Long> {
    fun deleteAllByMember(member: Member)
    fun findByMemberAndQuestionId(member: Member, questionId: Long): QuizQuestionStat?

    @Query(
        """
            SELECT q.stage.stageNumber AS stageNumber,
                   SUM(stat.attempts) AS attempts,
                   SUM(stat.correct) AS correct
            FROM QuizQuestionStat stat
            JOIN stat.question q
            WHERE stat.member = :member
            GROUP BY q.stage.stageNumber
        """
    )
    fun findStageAccuracySummaries(member: Member): List<QuizStageAccuracyProjection>

    @Query(
        """
            SELECT stat
            FROM QuizQuestionStat stat
            JOIN stat.question q
            WHERE stat.member = :member
              AND q.stage.stageNumber = :stageNumber
        """
    )
    fun findByMemberAndStageNumber(member: Member, stageNumber: Int): List<QuizQuestionStat>
}

interface QuizStageAccuracyProjection {
    val stageNumber: Int
    val attempts: Long
    val correct: Long
}
