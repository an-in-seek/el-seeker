package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizStage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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
            SELECT
                stage.stageNumber AS stageNumber,
                question.id AS questionId,
                question.questionText AS questionText,
                option.optionIndex AS optionIndex,
                option.optionText AS optionText
            FROM QuizStage stage
            LEFT JOIN stage._questions question
            LEFT JOIN question._options option
            WHERE stage.stageNumber = :stageNumber
            ORDER BY question.id, option.optionIndex
        """
    )
    fun findStageDetailRows(stageNumber: Int): List<QuizStageDetailRowProjection>

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

    @Modifying
    @Query(
        """
        UPDATE QuizStage stage
        SET stage.stageNumber = :stageNumber,
            stage.title = :title
        WHERE stage.id = :id
        """
    )
    fun updateStage(
        @Param("id") id: Long,
        @Param("stageNumber") stageNumber: Int,
        @Param("title") title: String?
    ): Int
}

interface QuizStageSummaryProjection {
    val stageNumber: Int
    val questionCount: Long
}

interface QuizStageDetailRowProjection {
    val stageNumber: Int
    val questionId: Long?
    val questionText: String?
    val optionIndex: Int?
    val optionText: String?
}
