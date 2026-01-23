package com.elseeker.game.application.service

import com.elseeker.common.IntegrationTest
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.ServiceError
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionCreateRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionUpdateRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingVerseProgressRequest
import com.elseeker.game.adapter.output.jpa.BibleTypingSessionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
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
) : IntegrationTest() {

    @Nested
    @DisplayName("createSession_메서드는")
    inner class createSession_메서드는 {

        @Test
        @DisplayName("새로운 세션을 생성한다")
        fun createSession() {
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
            assertThat(session).isNotNull
            assertThat(session.member.id).isEqualTo(member.id)
            assertThat(session.totalVerses).isEqualTo(10)
            assertThat(session.startedAt).isNotNull
        }
    }

    @Nested
    @DisplayName("updateSession_메서드는")
    inner class updateSession_메서드는 {

        @Test
        @DisplayName("세션 통계를 업데이트한다")
        fun updateSession() {
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
            assertThat(updatedSession.completedVerses).isEqualTo(5)
            assertThat(updatedSession.totalElapsedSeconds).isEqualTo(120)
            assertThat(updatedSession.endedAt).isNotNull
        }

        @Test
        @DisplayName("존재하지 않는 세션이면 예외가 발생한다")
        fun updateSession_not_found() {
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
            val exception = assertThrows(ServiceError::class.java) {
                bibleTypingSessionService.updateSession(member, UUID.randomUUID().toString(), updateRequest)
            }
            assertThat(exception.errorType).isEqualTo(ErrorType.SESSION_NOT_FOUND)
        }

        @Test
        @DisplayName("이미 종료된 세션이면 예외가 발생한다")
        fun updateSession_already_ended() {
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
            // 종료 상태로 만듦
            val updateRequestEnded = BibleTypingSessionUpdateRequest(
                totalVerses = 10,
                completedVerses = 10,
                totalTypedChars = 100,
                accuracy = 100.0,
                cpm = 400.0,
                totalElapsedSeconds = 100,
                endedAt = Instant.now(),
            )
            bibleTypingSessionService.updateSession(member, session.sessionKey.toString(), updateRequestEnded)

            // when & then
            val exception = assertThrows(ServiceError::class.java) {
                bibleTypingSessionService.updateSession(member, session.sessionKey.toString(), updateRequestEnded)
            }
            assertThat(exception.errorType).isEqualTo(ErrorType.SESSION_ALREADY_ENDED)
        }
    }

    @Nested
    @DisplayName("saveVerseProgress_메서드는")
    inner class saveVerseProgress_메서드는 {

        @Test
        @DisplayName("구절 진행 상황을 저장하고 세션 총 시간을 누적한다")
        fun saveVerseProgress_accumulates_duration() {
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

            // when: 첫 번째 구절 저장 (10초 소요)
            bibleTypingSessionService.saveVerseProgress(member, request1)

            // then
            val sessionUid = UUID.fromString(request1.sessionKey)
            val session1 = sessionRepository.findBySessionKeyAndMember(sessionUid, member)!!
            assertThat(session1.totalElapsedSeconds).isEqualTo(10)

            // given: 두 번째 구절 (5초 소요)
            val request2 = request1.copy(
                verseNumber = 2,
                startedAt = Instant.now().minusSeconds(5),
                endedAt = Instant.now()
            )

            // when
            bibleTypingSessionService.saveVerseProgress(member, request2)

            // then
            val session2 = sessionRepository.findBySessionKeyAndMember(sessionUid, member)!!
            assertThat(session2.totalElapsedSeconds).isEqualTo(15) // 10 + 5
        }

        @Test
        @DisplayName("기존 구절을 수정하면 총 시간이 조정된다")
        fun saveVerseProgress_updates_duration() {
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

            // when: 동일 구절을 20초 소요된 것으로 업데이트
            val updateRequest = request.copy(
                startedAt = Instant.now().minusSeconds(20),
                endedAt = Instant.now()
            )
            bibleTypingSessionService.saveVerseProgress(member, updateRequest)

            // then
            val sessionUid = UUID.fromString(request.sessionKey)
            val session = sessionRepository.findBySessionKeyAndMember(sessionUid, member)!!
            assertThat(session.totalElapsedSeconds).isEqualTo(20) // 20 (not 30)
        }

        @Test
        @DisplayName("이미 종료된 세션이면 예외가 발생한다")
        fun saveVerseProgress_already_ended() {
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
                startedAt = Instant.now(),
                endedAt = Instant.now(),
                completed = true,
            )
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
                member = member,
                sessionKey = session.sessionKey.toString(),
                request = BibleTypingSessionUpdateRequest(
                    totalVerses = 10,
                    completedVerses = 10,
                    totalTypedChars = 100,
                    accuracy = 100.0,
                    cpm = 100.0,
                    totalElapsedSeconds = 100,
                    endedAt = Instant.now(),
                )
            )
            val verseRequest = request.copy(sessionKey = session.sessionKey.toString())

            // when & then
            val exception = assertThrows(ServiceError::class.java) {
                bibleTypingSessionService.saveVerseProgress(member, verseRequest)
            }
            assertThat(exception.errorType).isEqualTo(ErrorType.SESSION_ALREADY_ENDED)
        }
    }

    @Nested
    @DisplayName("getProgress_메서드는")
    inner class getProgress_메서드는 {
        @Test
        @DisplayName("세션 정보를 조회한다")
        fun getProgress() {
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
            val session = bibleTypingSessionService.createSession(
                member = member,
                request = request,
            )

            // when
            val response = bibleTypingSessionService.getProgress(
                member = member,
                translationId = request.translationId,
                bookOrder = request.bookOrder,
                chapterNumber = request.chapterNumber,
            )

            // then
            assertThat(response).isNotNull
            assertThat(response?.sessionKey).isEqualTo(session.sessionKey.toString())
        }
    }
}
