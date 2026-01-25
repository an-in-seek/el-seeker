package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizStage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QuizStageRepository : JpaRepository<QuizStage, Long> {

    fun existsByStageNumber(stageNumber: Int): Boolean

    @Query(
        """
            SELECT DISTINCT stage
            FROM QuizStage stage
            LEFT JOIN FETCH stage._questions question
            WHERE stage.stageNumber = :stageNumber
        """
    )
    fun findByStageNumber(stageNumber: Int): QuizStage?

    @Query(
        """
            SELECT stage.stageNumber AS stageNumber, COUNT(question) AS questionCount
            FROM QuizStage stage
            LEFT JOIN stage._questions question
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
