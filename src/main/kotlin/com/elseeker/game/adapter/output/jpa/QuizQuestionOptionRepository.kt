package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizQuestionOption
import org.springframework.data.jpa.repository.JpaRepository

interface QuizQuestionOptionRepository : JpaRepository<QuizQuestionOption, Long> {
    fun deleteByQuestionId(questionId: Long)
}
