package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizStage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QuizStageRepository : JpaRepository<QuizStage, Long> {

    @Query(
        """
            SELECT DISTINCT stage
            FROM QuizStage stage
            LEFT JOIN FETCH stage.questions question
            LEFT JOIN FETCH question.options
            WHERE stage.stageNumber = :stageNumber
        """
    )
    fun findByStageNumber(stageNumber: Int): QuizStage?

    @Query(
        """
            SELECT stage.stageNumber AS stageNumber, COUNT(question) AS questionCount
            FROM QuizStage stage
            LEFT JOIN stage.questions question
            GROUP BY stage.id, stage.stageNumber
            ORDER BY stage.stageNumber
        """
    )
    fun findStageSummaries(): List<QuizStageSummaryProjection>
}

interface QuizStageSummaryProjection {
    val stageNumber: Int
    val questionCount: Long
}
