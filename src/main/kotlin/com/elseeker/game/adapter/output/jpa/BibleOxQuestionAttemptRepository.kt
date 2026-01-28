package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleOxQuestionAttempt
import com.elseeker.game.domain.model.BibleOxStageAttempt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BibleOxQuestionAttemptRepository : JpaRepository<BibleOxQuestionAttempt, Long> {

    @Query(
        """
        SELECT qa FROM BibleOxQuestionAttempt qa
        WHERE qa.stageAttempt = :stageAttempt
        ORDER BY qa.answeredAt ASC
        """
    )
    fun findByStageAttempt(@Param("stageAttempt") stageAttempt: BibleOxStageAttempt): List<BibleOxQuestionAttempt>

    @Query(
        """
        SELECT qa FROM BibleOxQuestionAttempt qa
        WHERE qa.stageAttempt = :stageAttempt
        AND qa.question.id = :questionId
        """
    )
    fun findByStageAttemptAndQuestionId(
        @Param("stageAttempt") stageAttempt: BibleOxStageAttempt,
        @Param("questionId") questionId: Long
    ): BibleOxQuestionAttempt?

    @Query(
        """
        SELECT COUNT(qa) FROM BibleOxQuestionAttempt qa
        WHERE qa.stageAttempt = :stageAttempt
        AND qa.isCorrect = true
        """
    )
    fun countCorrectByStageAttempt(@Param("stageAttempt") stageAttempt: BibleOxStageAttempt): Long

    fun existsByStageAttemptAndQuestionId(stageAttempt: BibleOxStageAttempt, questionId: Long): Boolean
}
