package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleOxStage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BibleOxStageRepository : JpaRepository<BibleOxStage, Long> {

    fun findByStageNumber(stageNumber: Int): BibleOxStage?

    @Query(
        """
        SELECT s FROM BibleOxStage s
        LEFT JOIN FETCH s._questions
        WHERE s.stageNumber = :stageNumber
        """
    )
    fun findByStageNumberWithQuestions(stageNumber: Int): BibleOxStage?

    @Query(
        """
        SELECT s FROM BibleOxStage s
        ORDER BY s.stageNumber ASC
        """
    )
    fun findAllOrderByStageNumber(): List<BibleOxStage>

    fun existsByStageNumber(stageNumber: Int): Boolean
}
