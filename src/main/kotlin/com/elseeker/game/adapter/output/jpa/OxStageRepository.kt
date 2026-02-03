package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.OxStage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OxStageRepository : JpaRepository<OxStage, Long> {

    fun findByStageNumber(stageNumber: Int): OxStage?

    @Query(
        """
        SELECT s FROM OxStage s
        LEFT JOIN FETCH s._questions
        WHERE s.stageNumber = :stageNumber
        """
    )
    fun findByStageNumberWithQuestions(stageNumber: Int): OxStage?

    @Query(
        """
        SELECT s FROM OxStage s
        ORDER BY s.stageNumber ASC
        """
    )
    fun findAllOrderByStageNumber(): List<OxStage>

    fun existsByStageNumber(stageNumber: Int): Boolean

    @Modifying
    @Query(
        """
        UPDATE OxStage s
        SET s.stageNumber = :stageNumber,
            s.bookName = :bookName
        WHERE s.id = :id
        """
    )
    fun updateStage(
        @Param("id") id: Long,
        @Param("stageNumber") stageNumber: Int,
        @Param("bookName") bookName: String
    ): Int
}
