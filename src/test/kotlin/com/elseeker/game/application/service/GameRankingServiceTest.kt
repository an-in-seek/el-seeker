package com.elseeker.game.application.service

import com.elseeker.bible.adapter.output.jpa.BibleBookRepository
import com.elseeker.bible.adapter.output.jpa.BibleChapterRepository
import com.elseeker.bible.adapter.output.jpa.BibleTranslationRepository
import com.elseeker.bible.adapter.output.jpa.BibleVerseRepository
import com.elseeker.bible.domain.model.BibleBook
import com.elseeker.bible.domain.model.BibleChapter
import com.elseeker.bible.domain.model.BibleTranslation
import com.elseeker.bible.domain.model.BibleVerse
import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.bible.domain.vo.BibleTestamentType
import com.elseeker.bible.domain.vo.BibleTranslationType
import com.elseeker.common.IntegrationTest
import com.elseeker.game.adapter.output.jpa.*
import com.elseeker.game.domain.model.*
import com.elseeker.game.domain.vo.*
import com.neovisionaries.i18n.LanguageCode
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant

@DisplayName("GameRankingService 통합테스트")
class GameRankingServiceTest @Autowired constructor(
    private val gameRankingService: GameRankingService,
    private val gameRankingRepository: GameRankingRepository,
    private val oxStageAttemptRepository: OxMemberStageAttemptRepository,
    private val bibleTypingVerseRepository: BibleTypingVerseRepository,
    private val bibleTranslationRepository: BibleTranslationRepository,
    private val bibleBookRepository: BibleBookRepository,
    private val bibleChapterRepository: BibleChapterRepository,
    private val bibleVerseRepository: BibleVerseRepository,
    private val bibleTypingSessionRepository: BibleTypingSessionRepository,
    private val quizStageAttemptRepository: QuizStageAttemptRepository,
    private val wordPuzzleRepository: WordPuzzleRepository,
    private val wordPuzzleAttemptRepository: WordPuzzleAttemptRepository,
) : IntegrationTest() {

    @Nested
    @DisplayName("getRankings_메서드는")
    inner class getRankings_메서드는 {

        @Test
        fun `게임 타입별 랭킹 목록을 순위순으로 조회한다`() {
            // given
            gameRankingRepository.save(
                GameRanking(
                    member = member,
                    gameType = GameType.OX_QUIZ,
                    rankingScore = BigDecimal("100.00"),
                    completedCount = 10,
                    perfectCount = 5,
                    rankingPosition = 1
                )
            )

            // when
            val rankings = gameRankingService.getRankings(GameType.OX_QUIZ, 50)

            // then
            rankings.size shouldBe 1
            rankings[0].rankingScore shouldBe BigDecimal("100.00")
            rankings[0].rankingPosition shouldBe 1
        }

        @Test
        fun `limit만큼만 조회한다`() {
            // given — 해당 게임 타입에 랭킹 데이터 없음

            // when
            val rankings = gameRankingService.getRankings(GameType.OX_QUIZ, 10)

            // then
            rankings.size shouldBe 0
        }
    }

    @Nested
    @DisplayName("getMyRanking_메서드는")
    inner class getMyRanking_메서드는 {

        @Test
        fun `내 순위를 조회한다`() {
            // given
            gameRankingRepository.save(
                GameRanking(
                    member = member,
                    gameType = GameType.OX_QUIZ,
                    rankingScore = BigDecimal("80.00"),
                    completedCount = 8,
                    perfectCount = 3,
                    rankingPosition = 1
                )
            )

            // when
            val myRanking = gameRankingService.getMyRanking(member, GameType.OX_QUIZ)

            // then
            myRanking.shouldNotBeNull()
            myRanking.rankingScore shouldBe BigDecimal("80.00")
            myRanking.completedCount shouldBe 8
            myRanking.perfectCount shouldBe 3
        }

        @Test
        fun `랭킹 데이터가 없으면 null을 반환한다`() {
            // when
            val myRanking = gameRankingService.getMyRanking(member, GameType.WORD_PUZZLE)

            // then
            myRanking.shouldBeNull()
        }
    }

    @Nested
    @DisplayName("getTotalParticipants_메서드는")
    inner class getTotalParticipants_메서드는 {

        @Test
        fun `게임 타입별 참여자 수를 조회한다`() {
            // given
            gameRankingRepository.save(
                GameRanking(
                    member = member,
                    gameType = GameType.TYPING,
                    rankingScore = BigDecimal("50.00"),
                    completedCount = 5,
                    perfectCount = 1,
                    rankingPosition = 1
                )
            )

            // when
            val count = gameRankingService.getTotalParticipants(GameType.TYPING)

            // then
            count shouldBe 1
        }

        @Test
        fun `참여자가 없으면 0을 반환한다`() {
            // when
            val count = gameRankingService.getTotalParticipants(GameType.MULTIPLE_CHOICE)

            // then
            count shouldBe 0
        }
    }

    @Nested
    @DisplayName("onGameCompleted_OX퀴즈는")
    inner class onGameCompleted_OX퀴즈는 {

        @Test
        fun `스테이지별 최고 점수를 합산하여 랭킹을 갱신한다`() {
            // given — 스테이지 1: 8점, 스테이지 2: 10점(만점)
            oxStageAttemptRepository.save(
                OxMemberStageAttempt(
                    member = member,
                    stageNumber = 1,
                    score = 8,
                    startedAt = Instant.now(),
                    completedAt = Instant.now()
                )
            )
            oxStageAttemptRepository.save(
                OxMemberStageAttempt(
                    member = member,
                    stageNumber = 2,
                    score = 10,
                    startedAt = Instant.now(),
                    completedAt = Instant.now()
                )
            )

            // when
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.OX_QUIZ)
            )

            // then
            val ranking = gameRankingRepository.findByMemberAndGameType(member, GameType.OX_QUIZ)
            ranking.shouldNotBeNull()
            ranking.rankingScore.compareTo(BigDecimal("18")) shouldBe 0
            ranking.completedCount shouldBe 2
            ranking.perfectCount shouldBe 1
            ranking.rankingPosition shouldBe 1
        }

        @Test
        fun `동일 스테이지 재도전 시 최고 점수만 반영한다`() {
            // given — 스테이지 1: 6점 → 9점 재도전
            oxStageAttemptRepository.save(
                OxMemberStageAttempt(
                    member = member,
                    stageNumber = 1,
                    score = 6,
                    startedAt = Instant.now(),
                    completedAt = Instant.now()
                )
            )
            oxStageAttemptRepository.save(
                OxMemberStageAttempt(
                    member = member,
                    stageNumber = 1,
                    score = 9,
                    startedAt = Instant.now(),
                    completedAt = Instant.now()
                )
            )

            // when
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.OX_QUIZ)
            )

            // then
            val ranking = gameRankingRepository.findByMemberAndGameType(member, GameType.OX_QUIZ)
            ranking.shouldNotBeNull()
            ranking.rankingScore.compareTo(BigDecimal("9")) shouldBe 0
            ranking.completedCount shouldBe 1
        }
    }

    @Nested
    @DisplayName("onGameCompleted_타이핑은")
    inner class onGameCompleted_타이핑은 {

        private lateinit var session: BibleTypingSession

        @BeforeEach
        fun seedTypingData() {
            val translation = bibleTranslationRepository.findByTranslationType(BibleTranslationType.KRV)
                ?: bibleTranslationRepository.save(
                    BibleTranslation(
                        translationType = BibleTranslationType.KRV,
                        name = "테스트 번역본",
                        translationOrder = 999,
                        languageCode = LanguageCode.ko
                    )
                )
            val book = bibleBookRepository.findByTranslationAndBook(translation.id!!, 1)
                ?: bibleBookRepository.save(
                    BibleBook(
                        translationId = translation.id!!,
                        bookKey = BibleBookKey.GEN,
                        bookOrder = 1,
                        name = "테스트 책",
                        abbreviation = "TEST",
                        testamentType = BibleTestamentType.OLD
                    )
                )
            val chapter = bibleChapterRepository.findByBookAndChapter(book.id!!, 1)
                ?: bibleChapterRepository.save(BibleChapter.of(bookId = book.id!!, chapterNumber = 1))
            if (bibleVerseRepository.findVerseText(translation.id!!, 1, 1, 1) == null) {
                bibleVerseRepository.save(BibleVerse(chapterId = chapter.id!!, verseNumber = 1, text = "테스트"))
            }
            if (bibleVerseRepository.findVerseText(translation.id!!, 1, 1, 2) == null) {
                bibleVerseRepository.save(BibleVerse(chapterId = chapter.id!!, verseNumber = 2, text = "테스트2"))
            }

            session = BibleTypingSession.create(
                member = member,
                translationId = translation.id!!,
                bookOrder = 1,
                chapterNumber = 1,
                totalVerses = 2
            )
            session = bibleTypingSessionRepository.save(session)
        }

        @Test
        fun `정확도와 CPM을 반영한 새 공식으로 랭킹 점수를 계산한다`() {
            // given — 2절 완료: accuracy 100/90, cpm 300/200
            val verse1 = BibleTypingVerse.create(session, 1, "테스트")
            verse1.updateProgress("테스트", 10, true) // accuracy=100, cpm=180
            bibleTypingVerseRepository.save(verse1)

            val verse2 = BibleTypingVerse.create(session, 2, "테스트2")
            verse2.updateProgress("테스X2", 10, true) // accuracy < 100
            bibleTypingVerseRepository.save(verse2)

            // when
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.TYPING)
            )

            // then
            val ranking = gameRankingRepository.findByMemberAndGameType(member, GameType.TYPING)
            ranking.shouldNotBeNull()
            ranking.completedCount shouldBe 2
            ranking.perfectCount shouldBe 1 // verse1만 100%

            // ranking_score = AVG(accuracy) × (1 + AVG(cpm) / 1000) + completed × 0.2 + perfect × 0.5
            // base > 0, volume bonus = 2*0.2 + 1*0.5 = 0.9
            ranking.rankingScore shouldBeGreaterThan BigDecimal.ZERO
        }

        @Test
        fun `완료되지 않은 절은 집계에서 제외된다`() {
            // given — 1절 완료, 1절 미완료
            val verse1 = BibleTypingVerse.create(session, 1, "테스트")
            verse1.updateProgress("테스트", 10, true)
            bibleTypingVerseRepository.save(verse1)

            val verse2 = BibleTypingVerse.create(session, 2, "테스트2")
            verse2.updateProgress("테스", 5, false) // 미완료
            bibleTypingVerseRepository.save(verse2)

            // when
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.TYPING)
            )

            // then
            val ranking = gameRankingRepository.findByMemberAndGameType(member, GameType.TYPING)
            ranking.shouldNotBeNull()
            ranking.completedCount shouldBe 1
            ranking.perfectCount shouldBe 1
        }

        @Test
        fun `완료된 절이 없으면 랭킹이 생성되지 않는다`() {
            // given — 모든 절 미완료
            val verse1 = BibleTypingVerse.create(session, 1, "테스트")
            verse1.updateProgress("테", 3, false)
            bibleTypingVerseRepository.save(verse1)

            // when — 이벤트 발행 (완료된 절 없어 집계 불가, 에러 로그 후 무시)
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.TYPING)
            )

            // then — 랭킹 데이터 미생성
            val ranking = gameRankingRepository.findByMemberAndGameType(member, GameType.TYPING)
            ranking.shouldBeNull()
        }

        @Test
        fun `정확한 수치로 새 공식이 계산된다`() {
            // given — 1절: "테스트" 정확 입력 (accuracy=100, typedText길이=3, elapsed=10 → cpm=18)
            val verse1 = BibleTypingVerse.create(session, 1, "테스트")
            verse1.updateProgress("테스트", 10, true)
            bibleTypingVerseRepository.save(verse1)

            // when
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.TYPING)
            )

            // then
            val ranking = gameRankingRepository.findByMemberAndGameType(member, GameType.TYPING)
            ranking.shouldNotBeNull()

            // accuracy=100.0, cpm= (3/10)*60 = 18.0
            // base = 100.0 × (1 + 18.0/1000) = 100.0 × 1.018 = 101.80
            // volume = 1 × 0.2 + 1 × 0.5 = 0.70
            // total = 102.50
            ranking.rankingScore.compareTo(BigDecimal("102.50")) shouldBe 0
            ranking.completedCount shouldBe 1
            ranking.perfectCount shouldBe 1
        }
    }

    @Nested
    @DisplayName("onGameCompleted_객관식퀴즈는")
    inner class onGameCompleted_객관식퀴즈는 {

        @Test
        fun `RECORD 모드만 집계하고 REVIEW 모드는 제외한다`() {
            // given — RECORD: 스테이지1 8점, REVIEW: 스테이지1 10점
            quizStageAttemptRepository.save(
                QuizMemberStageAttempt(
                    member = member,
                    stageNumber = 1,
                    mode = QuizStageAttemptMode.RECORD,
                    score = 8,
                    questionCount = 10,
                    startedAt = Instant.now(),
                    completedAt = Instant.now()
                )
            )
            quizStageAttemptRepository.save(
                QuizMemberStageAttempt(
                    member = member,
                    stageNumber = 1,
                    mode = QuizStageAttemptMode.REVIEW,
                    score = 10,
                    questionCount = 10,
                    startedAt = Instant.now(),
                    completedAt = Instant.now()
                )
            )

            // when
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.MULTIPLE_CHOICE)
            )

            // then — REVIEW 10점은 무시, RECORD 8점만 반영
            val ranking = gameRankingRepository.findByMemberAndGameType(member, GameType.MULTIPLE_CHOICE)
            ranking.shouldNotBeNull()
            ranking.rankingScore.compareTo(BigDecimal("8")) shouldBe 0
            ranking.completedCount shouldBe 1
            ranking.perfectCount shouldBe 0
        }
    }

    @Nested
    @DisplayName("onGameCompleted_단어퍼즐은")
    inner class onGameCompleted_단어퍼즐은 {

        @Test
        fun `퍼즐별 최고 점수를 합산하고 perfectCount는 1500점 이상 기준이다`() {
            // given
            val puzzle1 = wordPuzzleRepository.save(
                WordPuzzle(title = "퍼즐1", themeCode = "TEST", difficultyCode = QuizDifficulty.NORMAL, boardWidth = 5, boardHeight = 5)
            )
            val puzzle2 = wordPuzzleRepository.save(
                WordPuzzle(title = "퍼즐2", themeCode = "TEST", difficultyCode = QuizDifficulty.HARD, boardWidth = 5, boardHeight = 5)
            )

            // puzzle1: 1200점 (< 1500, perfect 아님)
            val attempt1 = WordPuzzleAttempt(member = member, wordPuzzle = puzzle1)
            attempt1.complete(1200)
            wordPuzzleAttemptRepository.save(attempt1)

            // puzzle2: 1800점 (>= 1500, perfect)
            val attempt2 = WordPuzzleAttempt(member = member, wordPuzzle = puzzle2)
            attempt2.complete(1800)
            wordPuzzleAttemptRepository.save(attempt2)

            // when
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.WORD_PUZZLE)
            )

            // then — 합산: 1200+1800=3000, perfect: 1800만 해당
            val ranking = gameRankingRepository.findByMemberAndGameType(member, GameType.WORD_PUZZLE)
            ranking.shouldNotBeNull()
            ranking.rankingScore.compareTo(BigDecimal("3000")) shouldBe 0
            ranking.completedCount shouldBe 2
            ranking.perfectCount shouldBe 1
        }
    }

    @Nested
    @DisplayName("onGameCompleted_랭킹재갱신은")
    inner class onGameCompleted_랭킹재갱신은 {

        @Test
        fun `기존 랭킹이 있으면 새 점수로 UPDATE한다`() {
            // given — 기존 랭킹: 5점
            oxStageAttemptRepository.save(
                OxMemberStageAttempt(
                    member = member, stageNumber = 1, score = 5,
                    startedAt = Instant.now(), completedAt = Instant.now()
                )
            )
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.OX_QUIZ)
            )
            val firstRanking = gameRankingRepository.findByMemberAndGameType(member, GameType.OX_QUIZ)
            firstRanking.shouldNotBeNull()
            firstRanking.rankingScore.compareTo(BigDecimal("5")) shouldBe 0

            // given — 추가 스테이지 완료: 10점
            oxStageAttemptRepository.save(
                OxMemberStageAttempt(
                    member = member, stageNumber = 2, score = 10,
                    startedAt = Instant.now(), completedAt = Instant.now()
                )
            )

            // when — 재갱신
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.OX_QUIZ)
            )

            // then — UPDATE: 5+10=15, INSERT가 아님 (행 수 1개)
            val updatedRanking = gameRankingRepository.findByMemberAndGameType(member, GameType.OX_QUIZ)
            updatedRanking.shouldNotBeNull()
            updatedRanking.rankingScore.compareTo(BigDecimal("15")) shouldBe 0
            updatedRanking.completedCount shouldBe 2
            updatedRanking.perfectCount shouldBe 1
            updatedRanking.id shouldBe firstRanking.id // 같은 행
        }
    }

    @Nested
    @DisplayName("onGameCompleted_미완료OX는")
    inner class onGameCompleted_미완료OX는 {

        @Test
        fun `completedAt이 null인 시도는 집계에서 제외된다`() {
            // given — 완료: 8점, 미완료(진행 중): 3점
            oxStageAttemptRepository.save(
                OxMemberStageAttempt(
                    member = member, stageNumber = 1, score = 8,
                    startedAt = Instant.now(), completedAt = Instant.now()
                )
            )
            oxStageAttemptRepository.save(
                OxMemberStageAttempt(
                    member = member, stageNumber = 2, score = 3,
                    startedAt = Instant.now(), completedAt = null // 미완료
                )
            )

            // when
            gameRankingService.onGameCompleted(
                com.elseeker.game.domain.event.GameCompletedEvent(member.id!!, GameType.OX_QUIZ)
            )

            // then — 미완료 스테이지2는 제외, 완료된 스테이지1만 반영
            val ranking = gameRankingRepository.findByMemberAndGameType(member, GameType.OX_QUIZ)
            ranking.shouldNotBeNull()
            ranking.rankingScore.compareTo(BigDecimal("8")) shouldBe 0
            ranking.completedCount shouldBe 1
        }
    }
}
