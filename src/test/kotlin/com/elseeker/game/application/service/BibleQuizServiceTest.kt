package com.elseeker.game.application.service

import com.elseeker.common.IntegrationTest
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.ServiceError
import com.elseeker.game.adapter.input.api.client.request.QuizStageAnswerRequest
import com.elseeker.game.adapter.input.api.client.request.QuizStageCompleteRequest
import com.elseeker.game.adapter.input.api.client.request.QuizStageStartRequest
import com.elseeker.game.adapter.output.jpa.*
import com.elseeker.game.domain.model.QuizQuestion
import com.elseeker.game.domain.model.QuizQuestionOption
import com.elseeker.game.domain.model.QuizStage
import com.elseeker.game.domain.vo.QuizStageAttemptMode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

@DisplayName("BibleQuizService 통합테스트")
class BibleQuizServiceTest @Autowired constructor(
    private val bibleQuizService: BibleQuizService,
    private val quizStageRepository: QuizStageRepository,
    private val quizQuestionRepository: QuizQuestionRepository,
    private val quizStageProgressRepository: QuizStageProgressRepository,
    private val quizProgressRepository: QuizProgressRepository,
    private val quizQuestionStatRepository: QuizQuestionStatRepository,
    private val quizStageAttemptRepository: QuizStageAttemptRepository
) : IntegrationTest() {

    private lateinit var stage1: QuizStage
    private lateinit var stage2: QuizStage

    @BeforeEach
    fun seedStages() {
        stage1 = seedStage(1, 1)
        stage2 = seedStage(2, 1)
    }

    @Nested
    @DisplayName("startStage_메서드는")
    inner class StartStage {

        @Test
        fun `record 모드 시작 시 진행 상태와 attempt를 생성한다`() {
            // when
            startStageRecord(stage1.stageNumber)

            // then
            val stageProgress = quizStageProgressRepository.findByMemberAndStageNumber(member, stage1.stageNumber)
            stageProgress.shouldNotBeNull()
            stageProgress.currentScore shouldBe 0
            stageProgress.currentQuestionIndex shouldBe 0
            stageProgress.currentReviewType shouldBe null

            val attempts = quizStageAttemptRepository.findAllByMember(member)
                .filter { it.stageNumber == stage1.stageNumber && it.mode == QuizStageAttemptMode.RECORD && it.completedAt == null }
            attempts.size shouldBe 1
        }

        @Test
        fun `record 모드 재시작 시 진행 중 attempt를 재사용한다`() {
            // given
            repeat(2) { startStageRecord(stage1.stageNumber) }

            // then
            val attempts = quizStageAttemptRepository.findAllByMember(member)
                .filter { it.stageNumber == stage1.stageNumber && it.mode == QuizStageAttemptMode.RECORD && it.completedAt == null }
            attempts.size shouldBe 1
        }

        @Test
        fun `review 모드 재시작 시 진행 중 attempt를 재사용한다`() {
            // given
            repeat(2) { startStageReview(stage1.stageNumber) }

            // then
            val attempts = quizStageAttemptRepository.findAllByMember(member)
                .filter { it.stageNumber == stage1.stageNumber && it.mode == QuizStageAttemptMode.REVIEW && it.completedAt == null }
            attempts.size shouldBe 1
        }

        @Test
        fun `review 모드 시작 시 점수는 0으로 초기화된다`() {
            // when
            startStageReview(stage1.stageNumber)

            // then
            val stageProgress = quizStageProgressRepository.findByMemberAndStageNumber(member, stage1.stageNumber)!!
            stageProgress.currentScore shouldBe 0
        }
    }

    @Nested
    @DisplayName("submitAnswer_메서드는")
    inner class SubmitAnswer {

        @Test
        fun `정답 제출 시 점수와 통계를 갱신한다`() {
            // given
            startStageRecord(stage1.stageNumber)
            val question = firstQuestion(stage1)

            // when
            val response = bibleQuizService.submitAnswer(
                stageNumber = stage1.stageNumber,
                request = QuizStageAnswerRequest(
                    questionId = question.id!!,
                    selectedIndex = 1,
                    questionIndex = 0,
                    mode = QuizStageAttemptMode.RECORD,
                    answeredAt = Instant.now()
                ),
                memberUid = member.uid
            )

            // then
            response.isCorrect shouldBe true
            response.currentScore shouldBe 1
            response.currentQuestionIndex shouldBe 1

            val stageProgress = quizStageProgressRepository.findByMemberAndStageNumber(member, stage1.stageNumber)!!
            stageProgress.currentScore shouldBe 1
            stageProgress.currentQuestionIndex shouldBe 1

            val stat = quizQuestionStatRepository.findByMemberAndQuestionId(member, question.id!!)!!
            stat.attempts shouldBe 1
            stat.correct shouldBe 1
        }

        @Test
        fun `이미 답한 문제는 예외가 발생한다`() {
            // given
            startStageRecord(stage1.stageNumber)
            val question = firstQuestion(stage1)
            val request = QuizStageAnswerRequest(
                questionId = question.id!!,
                selectedIndex = 1,
                questionIndex = 0,
                mode = QuizStageAttemptMode.RECORD,
                answeredAt = Instant.now()
            )
            bibleQuizService.submitAnswer(stage1.stageNumber, request, member.uid)

            // when & then
            val exception = shouldThrow<ServiceError> {
                bibleQuizService.submitAnswer(stage1.stageNumber, request, member.uid)
            }
            exception.errorType shouldBe ErrorType.INVALID_PARAMETER
        }

        @Test
        fun `현재 인덱스보다 큰 문제 번호를 제출하면 예외가 발생한다`() {
            // given
            startStageRecord(stage1.stageNumber)
            val question = firstQuestion(stage1)

            // when & then
            val exception = shouldThrow<ServiceError> {
                bibleQuizService.submitAnswer(
                    stageNumber = stage1.stageNumber,
                    request = QuizStageAnswerRequest(
                        questionId = question.id!!,
                        selectedIndex = 1,
                        questionIndex = 2,
                        mode = QuizStageAttemptMode.RECORD,
                        answeredAt = Instant.now()
                    ),
                    memberUid = member.uid
                )
            }
            exception.errorType shouldBe ErrorType.INVALID_PARAMETER
        }

        @Test
        fun `다른 스테이지 문제를 제출하면 예외가 발생한다`() {
            // given
            startStageRecord(stage1.stageNumber)
            val otherQuestion = firstQuestion(stage2)

            // when & then
            val exception = shouldThrow<ServiceError> {
                bibleQuizService.submitAnswer(
                    stageNumber = stage1.stageNumber,
                    request = QuizStageAnswerRequest(
                        questionId = otherQuestion.id!!,
                        selectedIndex = 1,
                        questionIndex = 0,
                        mode = QuizStageAttemptMode.RECORD,
                        answeredAt = Instant.now()
                    ),
                    memberUid = member.uid
                )
            }
            exception.errorType shouldBe ErrorType.INVALID_PARAMETER
        }
    }

    @Nested
    @DisplayName("completeStage_메서드는")
    inner class CompleteStage {

        @Test
        fun `record 완료 시 진행도와 점수를 반영한다`() {
            // given
            startStageRecord(stage1.stageNumber)

            // when
            val response = bibleQuizService.completeStage(
                stageNumber = stage1.stageNumber,
                request = QuizStageCompleteRequest(
                    mode = QuizStageAttemptMode.RECORD,
                    score = 1,
                    questionCount = 1,
                    completedAt = Instant.now()
                ),
                memberUid = member.uid
            )

            // then
            response.nextStage shouldBe stage2.stageNumber
            val progress = quizProgressRepository.findByMember(member)!!
            progress.lastCompletedStage shouldBe stage1.stageNumber
            progress.currentStageNumber shouldBe stage2.stageNumber

            val stageProgress = quizStageProgressRepository.findByMemberAndStageNumber(member, stage1.stageNumber)!!
            stageProgress.lastScore shouldBe 1
            stageProgress.currentQuestionIndex shouldBe null
            stageProgress.currentScore shouldBe null
        }

        @Test
        fun `review 완료 시 복습 횟수만 증가한다`() {
            // given
            startStageReview(stage1.stageNumber)

            // when
            bibleQuizService.completeStage(
                stageNumber = stage1.stageNumber,
                request = QuizStageCompleteRequest(
                    mode = QuizStageAttemptMode.REVIEW,
                    score = 0,
                    questionCount = 1,
                    completedAt = Instant.now()
                ),
                memberUid = member.uid
            )

            // then
            val progress = quizProgressRepository.findByMember(member)!!
            progress.lastCompletedStage shouldBe 0
            progress.currentStageNumber shouldBe 1

            val stageProgress = quizStageProgressRepository.findByMemberAndStageNumber(member, stage1.stageNumber)!!
            stageProgress.reviewCount shouldBe 1
            stageProgress.lastScore shouldBe null
        }
    }

    @Nested
    @DisplayName("getStageSummaries_메서드는")
    inner class StageSummaries {

        @Test
        fun `신규 사용자는 1스테이지만 current로 노출된다`() {
            // when
            val response = bibleQuizService.getStageSummaries(member.uid)

            // then
            response.currentStage shouldBe 1
            response.lastCompletedStage shouldBe 0
            response.stages.first { it.stageNumber == 1 }.isCurrent shouldBe true
            response.stages.first { it.stageNumber == 2 }.isLocked shouldBe true
        }
    }

    @Nested
    @DisplayName("getStage_메서드는")
    inner class GetStage {

        @Test
        fun `완료된 스테이지는 reviewOnly로 표시된다`() {
            // given
            startStageRecord(stage1.stageNumber)
            bibleQuizService.completeStage(
                stageNumber = stage1.stageNumber,
                request = QuizStageCompleteRequest(
                    mode = QuizStageAttemptMode.RECORD,
                    score = 1,
                    questionCount = 1,
                    completedAt = Instant.now()
                ),
                memberUid = member.uid
            )

            // when
            val response = bibleQuizService.getStage(stage1.stageNumber, member.uid)

            // then
            response.progress.isCompleted shouldBe true
            response.progress.isReviewOnly shouldBe true
        }
    }


    // ---------- private methods ----------

    private fun seedStage(stageNumber: Int, questionCount: Int): QuizStage {
        val stage = QuizStage(stageNumber = stageNumber, title = "Stage $stageNumber")
        repeat(questionCount) { index ->
            val question = QuizQuestion(
                stage = stage,
                questionText = "Question ${stageNumber}-${index + 1}",
                answerIndex = 1
            )
            question.addOption(QuizQuestionOption(question = question, optionText = "A", optionIndex = 0))
            question.addOption(QuizQuestionOption(question = question, optionText = "B", optionIndex = 1))
            question.addOption(QuizQuestionOption(question = question, optionText = "C", optionIndex = 2))
            stage.addQuestion(question)
        }
        quizStageRepository.save(stage)
        return stage
    }

    private fun startStageRecord(stageNumber: Int) {
        bibleQuizService.startStage(
            stageNumber = stageNumber,
            request = QuizStageStartRequest(
                mode = QuizStageAttemptMode.RECORD,
                reviewType = null,
                startedAt = Instant.now()
            ),
            memberUid = member.uid
        )
    }

    private fun startStageReview(stageNumber: Int) {
        bibleQuizService.startStage(
            stageNumber = stageNumber,
            request = QuizStageStartRequest(
                mode = QuizStageAttemptMode.REVIEW,
                reviewType = "full",
                startedAt = Instant.now()
            ),
            memberUid = member.uid
        )
    }

    private fun firstQuestion(stage: QuizStage): QuizQuestion {
        val questionId = stage.questions.first().id ?: return stage.questions.first()
        return quizQuestionRepository.findById(questionId).orElseThrow()
    }
}
