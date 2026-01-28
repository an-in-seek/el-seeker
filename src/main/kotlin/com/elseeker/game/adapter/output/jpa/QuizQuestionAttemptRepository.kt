package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.QuizMemberQuestionAttempt
import com.elseeker.game.domain.model.QuizMemberStageAttempt
import com.elseeker.game.domain.model.QuizQuestion
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuizQuestionAttemptRepository : JpaRepository<QuizMemberQuestionAttempt, Long> {
    fun findByStageAttemptAndQuestion(stageAttempt: QuizMemberStageAttempt, question: QuizQuestion): QuizMemberQuestionAttempt?

    @Modifying(clearAutomatically = true)
    @Query("delete from QuizMemberQuestionAttempt q where q.stageAttempt.member = :member")
    fun deleteAllByMember(@Param("member") member: Member)
}
