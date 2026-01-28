package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleOxQuestion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BibleOxQuestionRepository : JpaRepository<BibleOxQuestion, Long> {

    @Query(
        """
        SELECT q FROM BibleOxQuestion q
        WHERE q.stage.stageNumber = :stageNumber
        ORDER BY q.orderIndex ASC
        """
    )
    fun findByStageNumber(@Param("stageNumber") stageNumber: Int): List<BibleOxQuestion>

    @Query(
        """
        SELECT q FROM BibleOxQuestion q
        JOIN FETCH q.stage
        WHERE q.id = :questionId
        """
    )
    fun findByIdWithStage(@Param("questionId") questionId: Long): BibleOxQuestion?

    @Query(
        """
        SELECT COUNT(q) FROM BibleOxQuestion q
        WHERE q.stage.stageNumber = :stageNumber
        """
    )
    fun countByStageNumber(@Param("stageNumber") stageNumber: Int): Long

    @Query(
        """
        SELECT q.stage.stageNumber AS stageNumber, COUNT(q) AS totalQuestions
        FROM BibleOxQuestion q
        GROUP BY q.stage.stageNumber
        """
    )
    fun countByStageNumberGroup(): List<StageQuestionCountRow>
}

interface StageQuestionCountRow {
    val stageNumber: Int
    val totalQuestions: Long
}
