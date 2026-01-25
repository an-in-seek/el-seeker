package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizQuestion
import com.elseeker.game.domain.model.QuizQuestionAttempt
import com.elseeker.game.domain.model.QuizStageAttempt
import org.springframework.data.jpa.repository.JpaRepository

interface QuizQuestionAttemptRepository : JpaRepository<QuizQuestionAttempt, Long> {
    fun findByStageAttemptAndQuestion(stageAttempt: QuizStageAttempt, question: QuizQuestion): QuizQuestionAttempt?
}
