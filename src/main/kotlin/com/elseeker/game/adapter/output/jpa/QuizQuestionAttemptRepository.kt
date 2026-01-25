package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizQuestion
import com.elseeker.game.domain.model.QuizQuestionAttempt
import com.elseeker.game.domain.model.QuizStageAttempt
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuizQuestionAttemptRepository : JpaRepository<QuizQuestionAttempt, Long> {
    fun findByStageAttemptAndQuestion(stageAttempt: QuizStageAttempt, question: QuizQuestion): QuizQuestionAttempt?

    @Modifying(clearAutomatically = true)
    @Query("delete from QuizQuestionAttempt q where q.stageAttempt.member = :member")
    fun deleteAllByMember(@Param("member") member: Member)
}
