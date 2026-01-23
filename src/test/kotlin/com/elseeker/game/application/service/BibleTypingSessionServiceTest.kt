package com.elseeker.game.application.service

import com.elseeker.common.IntegrationTest
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.ServiceError
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionCreateRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionUpdateRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingVerseProgressRequest
import com.elseeker.game.adapter.output.jpa.BibleTypingSessionRepository
import com.elseeker.game.adapter.output.jpa.BibleTypingVerseRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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
) : IntegrationTest() {

    @Nested
    @DisplayName("createSession_메서드는")
    inner class CreateSession {

        @Test
        fun `새로운 세션을 생성한다`() {
            // given
            val request = BibleTypingSessionCreateRequest(
                sessionKey = UUID.randomUUID().toString(),
                translationId = 1L,
                bookOrder = 1,
                chapterNumber = 1,
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
    @DisplayName("updateSession_메서드는")
    inner class UpdateSession {

        @Test
        fun `세션 통계를 업데이트한다`() {
            // given
            val session = bibleTypingSessionService.createSession(
                member = member,
                request = BibleTypingSessionCreateRequest(
                    sessionKey = UUID.randomUUID().toString(),
                    translationId = 1L,
                    bookOrder = 1,
                    chapterNumber = 1,
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

            val updateRequest = BibleTypingSessionUpdateRequest(
                totalVerses = 10,
                completedVerses = 5,
                totalTypedChars = 100,
                accuracy = 95.5,
                cpm = 300.0,
                totalElapsedSeconds = 120,
                endedAt = Instant.now()
            )

            // when
            bibleTypingSessionService.updateSession(member, session.sessionKey.toString(), updateRequest)

            // then
            val updatedSession = sessionRepository.findById(session.id!!).get()
            updatedSession.completedVerses shouldBe 5
            updatedSession.totalElapsedSeconds shouldBe 120
            updatedSession.endedAt.shouldNotBeNull()
        }

        @Test
        fun `존재하지 않는 세션이면 예외가 발생한다`() {
            // given
            val updateRequest = BibleTypingSessionUpdateRequest(
                totalVerses = 10,
                completedVerses = 5,
                totalTypedChars = 100,
                accuracy = 95.5,
                cpm = 300.0,
                totalElapsedSeconds = 120,
                endedAt = Instant.now()
            )

            // when & then
            val exception = shouldThrow<ServiceError> {
                bibleTypingSessionService.updateSession(
                    member,
                    UUID.randomUUID().toString(),
                    updateRequest
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
                    translationId = 1L,
                    bookOrder = 1,
                    chapterNumber = 1,
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

            val endedRequest = BibleTypingSessionUpdateRequest(
                totalVerses = 10,
                completedVerses = 10,
                totalTypedChars = 100,
                accuracy = 100.0,
                cpm = 400.0,
                totalElapsedSeconds = 100,
                endedAt = Instant.now(),
            )

            bibleTypingSessionService.updateSession(member, session.sessionKey.toString(), endedRequest)

            // when & then
            val exception = shouldThrow<ServiceError> {
                bibleTypingSessionService.updateSession(member, session.sessionKey.toString(), endedRequest)
            }

            exception.errorType shouldBe ErrorType.SESSION_ALREADY_ENDED
        }
    }

    @Nested
    @DisplayName("saveVerseProgress_메서드는")
    inner class SaveVerseProgress {

        @Test
        fun `구절 진행 상황을 저장하고 세션 총 시간을 누적한다`() {
            // given
            val request1 = BibleTypingVerseProgressRequest(
                sessionKey = UUID.randomUUID().toString(),
                translationId = 1L,
                bookOrder = 1,
                chapterNumber = 1,
                verseNumber = 1,
                originalText = "태초에 하나님이...",
                typedText = "태초에 하나님이...",
                accuracy = 100.0,
                cpm = 400.0,
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
                verseNumber = 2,
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
                translationId = 1L,
                bookOrder = 1,
                chapterNumber = 1,
                verseNumber = 1,
                originalText = "Text",
                typedText = "Text",
                accuracy = 100.0,
                cpm = 400.0,
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
                    translationId = 1L,
                    bookOrder = 1,
                    chapterNumber = 1,
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

            bibleTypingSessionService.updateSession(
                member,
                session.sessionKey.toString(),
                BibleTypingSessionUpdateRequest(
                    totalVerses = 10,
                    completedVerses = 10,
                    totalTypedChars = 100,
                    accuracy = 100.0,
                    cpm = 100.0,
                    totalElapsedSeconds = 100,
                    endedAt = Instant.now(),
                )
            )

            val verseRequest = BibleTypingVerseProgressRequest(
                sessionKey = session.sessionKey.toString(),
                translationId = 1L,
                bookOrder = 1,
                chapterNumber = 1,
                verseNumber = 1,
                originalText = "Text",
                typedText = "Text",
                accuracy = 100.0,
                cpm = 400.0,
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
    inner class GetProgress {

        @Test
        fun `세션 정보를 조회한다`() {
            // given
            val request = BibleTypingSessionCreateRequest(
                sessionKey = UUID.randomUUID().toString(),
                translationId = 1L,
                bookOrder = 1,
                chapterNumber = 1,
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
    inner class ResetSession {

        @Test
        fun `세션을 초기화하면 세션과 구절 데이터가 모두 삭제된다`() {
            // given
            val sessionKey = UUID.randomUUID().toString()
            val request = BibleTypingSessionCreateRequest(
                sessionKey = sessionKey,
                translationId = 1L,
                bookOrder = 1,
                chapterNumber = 1,
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
                translationId = 1L,
                bookOrder = 1,
                chapterNumber = 1,
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
