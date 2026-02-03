package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizQuestion
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuizQuestionRepository : JpaRepository<QuizQuestion, Long> {
    @EntityGraph(attributePaths = ["stage"])
    fun findByStageId(stageId: Long, pageable: Pageable): Page<QuizQuestion>

    @Query(
        """
        SELECT q FROM QuizQuestion q
        JOIN FETCH q.stage
        LEFT JOIN FETCH q._options
        WHERE q.id = :id
        """
    )
    fun findWithOptionsById(@Param("id") id: Long): QuizQuestion?
}
