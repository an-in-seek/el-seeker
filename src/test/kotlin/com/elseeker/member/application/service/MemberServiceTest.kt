package com.elseeker.member.application.service

import com.elseeker.common.IntegrationTest
import com.elseeker.game.adapter.output.jpa.QuizQuestionAttemptRepository
import com.elseeker.game.adapter.output.jpa.QuizQuestionRepository
import com.elseeker.game.adapter.output.jpa.QuizStageAttemptRepository
import com.elseeker.game.adapter.output.jpa.QuizStageRepository
import com.elseeker.game.domain.model.QuizOption
import com.elseeker.game.domain.model.QuizQuestion
import com.elseeker.game.domain.model.QuizQuestionAttempt
import com.elseeker.game.domain.model.QuizStage
import com.elseeker.game.domain.model.QuizStageAttempt
import com.elseeker.game.domain.vo.QuizStageAttemptMode
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

@DisplayName("MemberService 통합테스트")
class MemberServiceTest @Autowired constructor(
    private val memberService: MemberService,
    private val quizStageRepository: QuizStageRepository,
    private val quizQuestionRepository: QuizQuestionRepository,
    private val quizStageAttemptRepository: QuizStageAttemptRepository,
    private val quizQuestionAttemptRepository: QuizQuestionAttemptRepository
) : IntegrationTest() {

    @Test
    fun `회원 삭제 시 퀴즈 시도와 문항 시도가 함께 삭제된다`() {
        // given
        val stage = quizStageRepository.save(
            QuizStage(stageNumber = 1, title = "테스트 스테이지")
        )
        val question = QuizQuestion(
            stage = stage,
            questionText = "테스트 문제",
            answerIndex = 0
        )
        question.addOption(
            QuizOption(
                question = question,
                optionText = "테스트 보기",
                optionIndex = 0
            )
        )
        val savedQuestion = quizQuestionRepository.save(question)

        val stageAttempt = quizStageAttemptRepository.save(
            QuizStageAttempt(
                member = member,
                stageNumber = stage.stageNumber,
                mode = QuizStageAttemptMode.RECORD,
                score = 0,
                questionCount = 1,
                startedAt = Instant.now()
            )
        )

        quizQuestionAttemptRepository.save(
            QuizQuestionAttempt(
                stageAttempt = stageAttempt,
                question = savedQuestion,
                selectedIndex = 0,
                isCorrect = true,
                answeredAt = Instant.now()
            )
        )

        quizStageAttemptRepository.findAllByMember(member).size shouldBe 1
        quizQuestionAttemptRepository.count() shouldBe 1

        // when
        memberService.deleteMember(member.uid, member.uid)

        // then
        quizQuestionAttemptRepository.count() shouldBe 0
        quizStageAttemptRepository.findAllByMember(member).shouldBeEmpty()
    }
}
