package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.OxQuestion
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OxQuestionRepository : JpaRepository<OxQuestion, Long> {

    @Query(
        """
        SELECT q FROM OxQuestion q
        WHERE q.stage.stageNumber = :stageNumber
        ORDER BY q.orderIndex ASC
        """
    )
    fun findByStageNumber(@Param("stageNumber") stageNumber: Int): List<OxQuestion>

    @Query(
        """
        SELECT q FROM OxQuestion q
        JOIN FETCH q.stage
        WHERE q.id = :questionId
        """
    )
    fun findByIdWithStage(@Param("questionId") questionId: Long): OxQuestion?

    @Query(
        """
        SELECT q FROM OxQuestion q
        JOIN FETCH q.stage
        WHERE q.id = :id
        """
    )
    fun findWithStageById(@Param("id") id: Long): OxQuestion?

    @Query(
        """
        SELECT COUNT(q) FROM OxQuestion q
        WHERE q.stage.stageNumber = :stageNumber
        """
    )
    fun countByStageNumber(@Param("stageNumber") stageNumber: Int): Long

    @EntityGraph(attributePaths = ["stage"])
    fun findByStageId(stageId: Long, pageable: Pageable): Page<OxQuestion>

    fun existsByStageIdAndOrderIndex(stageId: Long, orderIndex: Int): Boolean

    @Query(
        """
        SELECT COUNT(q) > 0 FROM OxQuestion q
        WHERE q.stage.id = :stageId
          AND q.orderIndex = :orderIndex
          AND q.id <> :excludeId
        """
    )
    fun existsByStageIdAndOrderIndexExcludingId(
        @Param("stageId") stageId: Long,
        @Param("orderIndex") orderIndex: Int,
        @Param("excludeId") excludeId: Long
    ): Boolean

    @Query(
        """
        SELECT q.stage.stageNumber AS stageNumber, COUNT(q) AS totalQuestions
        FROM OxQuestion q
        GROUP BY q.stage.stageNumber
        """
    )
    fun countByStageNumberGroup(): List<StageQuestionCountRow>
}

interface StageQuestionCountRow {
    val stageNumber: Int
    val totalQuestions: Long
}
