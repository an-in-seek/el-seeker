package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizQuestion
import org.springframework.data.jpa.repository.JpaRepository

interface QuizQuestionRepository : JpaRepository<QuizQuestion, Long>
