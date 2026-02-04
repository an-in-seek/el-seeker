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
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.ServiceError
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionCreateRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionEndRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingVerseProgressRequest
import com.elseeker.game.adapter.output.jpa.BibleTypingSessionRepository
import com.elseeker.game.adapter.output.jpa.BibleTypingVerseRepository
import com.neovisionaries.i18n.LanguageCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

@DisplayName("BibleTypingSessionService 통합테스트")
class BibleTypingSessionServiceTest @Autowired constructor(
    private val bibleTypingSessionService: BibleTypingSessionService,
    private val sessionRepository: BibleTypingSessionRepository,
    private val bibleTypingVerseRepository: BibleTypingVerseRepository,
    private val bibleTranslationRepository: BibleTranslationRepository,
    private val bibleBookRepository: BibleBookRepository,
    private val bibleChapterRepository: BibleChapterRepository,
    private val bibleVerseRepository: BibleVerseRepository,
) : IntegrationTest() {

    private lateinit var seed: SeedContext

    private data class SeedContext(
        val translationId: Long,
        val bookOrder: Int,
        val chapterNumber: Int,
        val verseNumber1: Int,
        val verseNumber2: Int
    )

    @BeforeEach
    fun ensureBibleSeed() {
        seed = seedBibleData()
    }

    private fun seedBibleData(): SeedContext {
        val translation = bibleTranslationRepository.findByTranslationType(BibleTranslationType.KRV)
            ?: bibleTranslationRepository.save(
                BibleTranslation(
                    translationType = BibleTranslationType.KRV,
                    name = "테스트 번역본",
                    translationOrder = 999,
                    languageCode = LanguageCode.ko
                )
            )
        val translationId = translation.id!!
        val bookOrder = 1
        val book = bibleBookRepository.findByTranslationAndBook(translationId, bookOrder)
            ?: bibleBookRepository.save(
                BibleBook(
                    translationId = translationId,
                    bookKey = BibleBookKey.GEN,
                    bookOrder = bookOrder,
                    name = "테스트 책",
                    abbreviation = "TEST",
                    testamentType = BibleTestamentType.OLD
                )
            )
        val chapterNumber = 1
        val chapter = bibleChapterRepository.findByBookAndChapter(book.id!!, chapterNumber)
            ?: bibleChapterRepository.save(
                BibleChapter.of(
                    bookId = book.id!!,
                    chapterNumber = chapterNumber
                )
            )
        val verseNumber1 = 1
        val verseNumber2 = 2
        val verseText1 = bibleVerseRepository.findVerseText(translationId, bookOrder, chapterNumber, verseNumber1)
        if (verseText1 == null) {
            bibleVerseRepository.save(
                BibleVerse(
                    chapterId = chapter.id!!,
                    verseNumber = verseNumber1,
                    text = "테스트 구절 1"
                )
            )
        }
        val verseText2 = bibleVerseRepository.findVerseText(translationId, bookOrder, chapterNumber, verseNumber2)
        if (verseText2 == null) {
            bibleVerseRepository.save(
                BibleVerse(
                    chapterId = chapter.id!!,
                    verseNumber = verseNumber2,
                    text = "테스트 구절 2"
                )
            )
        }

        return SeedContext(
            translationId = translationId,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber,
            verseNumber1 = verseNumber1,
            verseNumber2 = verseNumber2
        )
    }

    @Nested
    @DisplayName("createSession_메서드는")
    inner class createSession_메서드는 {

        @Test
        fun `새로운 세션을 생성한다`() {
            // given
            val request = BibleTypingSessionCreateRequest(
                sessionKey = UUID.randomUUID().toString(),
                translationId = seed.translationId,
                bookOrder = seed.bookOrder,
                chapterNumber = seed.chapterNumber,
                totalVerses = 10,
                startedAt = Instant.now(),
                endedAt = Instant.now(),
                completedVerses = 0,
                totalTypedChars = 0,
                accuracy = 0.0,
                cpm = 0.0,
                verses = emptyList(),
            )

            // when
            val session = bibleTypingSessionService.createSession(member, request)

            // then
            session.shouldNotBeNull()
            session.member.id shouldBe member.id
            session.totalVerses shouldBe 10
            session.startedAt.shouldNotBeNull()
        }
    }

    @Nested
    @DisplayName("endSession_메서드는")
    inner class endSession_메서드는 {

        @Test
        fun `세션 종료 시각을 업데이트한다`() {
            // given
            val session = bibleTypingSessionService.createSession(
                member = member,
                request = BibleTypingSessionCreateRequest(
                    sessionKey = UUID.randomUUID().toString(),
                    translationId = seed.translationId,
                    bookOrder = seed.bookOrder,
                    chapterNumber = seed.chapterNumber,
                    totalVerses = 10,
                    startedAt = Instant.now(),
                    endedAt = Instant.now(),
                    completedVerses = 0,
                    totalTypedChars = 0,
                    accuracy = 0.0,
                    cpm = 0.0,
                    verses = emptyList(),
                )
            )

            val endRequest = BibleTypingSessionEndRequest(
                endedAt = Instant.now()
            )

            // when
            bibleTypingSessionService.endSession(member, session.sessionKey.toString(), endRequest)

            // then
            val updatedSession = sessionRepository.findById(session.id!!).get()
            updatedSession.completedVerses shouldBe 0
            updatedSession.totalElapsedSeconds shouldBe 0
            updatedSession.endedAt.shouldNotBeNull()
        }

        @Test
        fun `존재하지 않는 세션이면 예외가 발생한다`() {
            // given
            val endRequest = BibleTypingSessionEndRequest(
                endedAt = Instant.now()
            )

            // when & then
            val exception = shouldThrow<ServiceError> {
                bibleTypingSessionService.endSession(
                    member,
                    UUID.randomUUID().toString(),
                    endRequest
                )
            }

            exception.errorType shouldBe ErrorType.SESSION_NOT_FOUND
        }

        @Test
        fun `이미 종료된 세션이면 예외가 발생한다`() {
            // given
            val session = bibleTypingSessionService.createSession(
                member = member,
                request = BibleTypingSessionCreateRequest(
                    sessionKey = UUID.randomUUID().toString(),
                    translationId = seed.translationId,
                    bookOrder = seed.bookOrder,
                    chapterNumber = seed.chapterNumber,
                    totalVerses = 10,
                    startedAt = Instant.now(),
                    endedAt = Instant.now(),
                    completedVerses = 0,
                    totalTypedChars = 0,
                    accuracy = 0.0,
                    cpm = 0.0,
                    verses = emptyList(),
                )
            )

            val endedRequest = BibleTypingSessionEndRequest(
                endedAt = Instant.now(),
            )

            bibleTypingSessionService.endSession(member, session.sessionKey.toString(), endedRequest)

            // when & then
            val exception = shouldThrow<ServiceError> {
                bibleTypingSessionService.endSession(member, session.sessionKey.toString(), endedRequest)
            }

            exception.errorType shouldBe ErrorType.SESSION_ALREADY_ENDED
        }
    }

    @Nested
    @DisplayName("saveVerseProgress_메서드는")
    inner class saveVerseProgress_메서드는 {

        @Test
        fun `구절 진행 상황을 저장하고 세션 총 시간을 누적한다`() {
            // given
            val request1 = BibleTypingVerseProgressRequest(
                sessionKey = UUID.randomUUID().toString(),
                translationId = seed.translationId,
                bookOrder = seed.bookOrder,
                chapterNumber = seed.chapterNumber,
                verseNumber = seed.verseNumber1,
                typedText = "테스트 입력",
                startedAt = Instant.now().minusSeconds(10),
                endedAt = Instant.now(),
                completed = true
            )

            // when
            bibleTypingSessionService.saveVerseProgress(member, request1)

            val sessionUid = UUID.fromString(request1.sessionKey)
            val session1 = sessionRepository.findBySessionKeyAndMember(sessionUid, member)!!
            session1.totalElapsedSeconds shouldBe 10

            // given
            val request2 = request1.copy(
                verseNumber = seed.verseNumber2,
                startedAt = Instant.now().minusSeconds(5),
                endedAt = Instant.now()
            )

            // when
            bibleTypingSessionService.saveVerseProgress(member, request2)

            // then
            val session2 = sessionRepository.findBySessionKeyAndMember(sessionUid, member)!!
            session2.totalElapsedSeconds shouldBe 15
        }

        @Test
        fun `기존 구절을 수정하면 총 시간이 조정된다`() {
            // given
            val request = BibleTypingVerseProgressRequest(
                sessionKey = UUID.randomUUID().toString(),
                translationId = seed.translationId,
                bookOrder = seed.bookOrder,
                chapterNumber = seed.chapterNumber,
                verseNumber = seed.verseNumber1,
                typedText = "테스트 입력",
                startedAt = Instant.now().minusSeconds(10),
                endedAt = Instant.now(),
                completed = true
            )

            bibleTypingSessionService.saveVerseProgress(member, request)

            // when
            val updateRequest = request.copy(
                startedAt = Instant.now().minusSeconds(20),
                endedAt = Instant.now()
            )
            bibleTypingSessionService.saveVerseProgress(member, updateRequest)

            // then
            val sessionUid = UUID.fromString(request.sessionKey)
            val session = sessionRepository.findBySessionKeyAndMember(sessionUid, member)!!
            session.totalElapsedSeconds shouldBe 20
        }

        @Test
        fun `이미 종료된 세션이면 예외가 발생한다`() {
            // given
            val session = bibleTypingSessionService.createSession(
                member = member,
                request = BibleTypingSessionCreateRequest(
                    sessionKey = UUID.randomUUID().toString(),
                    translationId = seed.translationId,
                    bookOrder = seed.bookOrder,
                    chapterNumber = seed.chapterNumber,
                    totalVerses = 10,
                    startedAt = Instant.now(),
                    endedAt = Instant.now(),
                    completedVerses = 0,
                    totalTypedChars = 0,
                    accuracy = 0.0,
                    cpm = 0.0,
                    verses = emptyList(),
                )
            )

            bibleTypingSessionService.endSession(
                member,
                session.sessionKey.toString(),
                BibleTypingSessionEndRequest(
                    endedAt = Instant.now(),
                )
            )

            val verseRequest = BibleTypingVerseProgressRequest(
                sessionKey = session.sessionKey.toString(),
                translationId = seed.translationId,
                bookOrder = seed.bookOrder,
                chapterNumber = seed.chapterNumber,
                verseNumber = seed.verseNumber1,
                typedText = "테스트 입력",
                startedAt = Instant.now(),
                endedAt = Instant.now(),
                completed = true,
            )

            // when & then
            val exception = shouldThrow<ServiceError> {
                bibleTypingSessionService.saveVerseProgress(member, verseRequest)
            }

            exception.errorType shouldBe ErrorType.SESSION_ALREADY_ENDED
        }
    }

    @Nested
    @DisplayName("getProgress_메서드는")
    inner class getProgress_메서드는 {

        @Test
        fun `세션 정보를 조회한다`() {
            // given
            val request = BibleTypingSessionCreateRequest(
                sessionKey = UUID.randomUUID().toString(),
                translationId = seed.translationId,
                bookOrder = seed.bookOrder,
                chapterNumber = seed.chapterNumber,
                totalVerses = 1,
                startedAt = Instant.now(),
                endedAt = Instant.now(),
                completedVerses = 0,
                totalTypedChars = 0,
                accuracy = 0.0,
                cpm = 0.0,
                verses = emptyList(),
            )

            val session = bibleTypingSessionService.createSession(member, request)

            // when
            val response = bibleTypingSessionService.getProgress(
                member = member,
                translationId = request.translationId,
                bookOrder = request.bookOrder,
                chapterNumber = request.chapterNumber,
            )

            // then
            response.shouldNotBeNull()
            response.sessionKey shouldBe session.sessionKey.toString()
        }
    }

    @Nested
    @DisplayName("resetSession_메서드는")
    inner class resetSession_메서드는 {

        @Test
        fun `세션을 초기화하면 세션과 구절 데이터가 모두 삭제된다`() {
            // given
            val sessionKey = UUID.randomUUID().toString()
            val request = BibleTypingSessionCreateRequest(
                sessionKey = sessionKey,
                translationId = seed.translationId,
                bookOrder = seed.bookOrder,
                chapterNumber = seed.chapterNumber,
                totalVerses = 10,
                startedAt = Instant.now(),
                endedAt = Instant.now(),
                completedVerses = 0,
                totalTypedChars = 0,
                accuracy = 0.0,
                cpm = 0.0,
                verses = emptyList(),
            )
            bibleTypingSessionService.createSession(member, request)

            // when
            bibleTypingSessionService.resetSession(
                member = member,
                translationId = seed.translationId,
                bookOrder = seed.bookOrder,
                chapterNumber = seed.chapterNumber,
            )

            // then
            val sessionUid = UUID.fromString(sessionKey)
            val session = sessionRepository.findBySessionKeyAndMember(sessionUid, member)
            session shouldBe null
            val verses = bibleTypingVerseRepository.findAllBySessionSessionKey(sessionUid)
            verses.shouldBeEmpty()
        }

        @Test
        fun `존재하지 않는 세션을 초기화하면 예외가 발생한다`() {
            // when & then
            val exception = shouldThrow<ServiceError> {
                bibleTypingSessionService.resetSession(
                    member = member,
                    translationId = 999L,
                    bookOrder = 999,
                    chapterNumber = 999,
                )
            }
            exception.errorType shouldBe ErrorType.SESSION_NOT_FOUND
        }
    }
}
