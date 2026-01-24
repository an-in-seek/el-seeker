package com.elseeker.game.application.service

import com.elseeker.bible.application.component.BibleReader
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionCreateRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionEndRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingVerseProgressRequest
import com.elseeker.game.adapter.input.api.response.BibleTypingVersesResponse
import com.elseeker.game.adapter.output.jpa.BibleTypingSessionRepository
import com.elseeker.game.adapter.output.jpa.BibleTypingVerseRepository
import com.elseeker.game.domain.model.BibleTypingSession
import com.elseeker.game.domain.model.BibleTypingVerse
import com.elseeker.game.domain.model.BibleTypingVerseId
import com.elseeker.member.domain.model.Member
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class BibleTypingSessionService(
    private val sessionRepository: BibleTypingSessionRepository,
    private val verseRepository: BibleTypingVerseRepository,
    private val bibleReader: BibleReader,
) {

    @Transactional
    fun createSession(
        member: Member,
        request: BibleTypingSessionCreateRequest
    ): BibleTypingSession {
        return BibleTypingSession.create(
            member = member,
            translationId = request.translationId,
            bookOrder = request.bookOrder,
            chapterNumber = request.chapterNumber,
            totalVerses = request.totalVerses
        ).let { sessionRepository.save(it) }
    }

    @Transactional
    fun endSession(
        member: Member,
        sessionKey: String,
        request: BibleTypingSessionEndRequest
    ) {
        val sessionUid = parseSessionUid(sessionKey)
        val session = sessionRepository.findBySessionKeyAndMember(sessionUid, member) ?: throwError(ErrorType.SESSION_NOT_FOUND)
        if (session.endedAt != null) throwError(ErrorType.SESSION_ALREADY_ENDED)
        session.end(request.endedAt)
    }

    @Transactional
    fun saveVerseProgress(
        member: Member,
        request: BibleTypingVerseProgressRequest
    ) {
        // 1. 세션 조회
        val session = getOrCreateSession(member, request)

        // 2. 세션 종료 여부 확인
        if (session.endedAt != null) throwError(ErrorType.SESSION_ALREADY_ENDED)

        // 3. 구절 조회
        val verseId = BibleTypingVerseId.of(sessionId = session.id!!, verseNumber = request.verseNumber)

        // 4. 구절 정보 조회, 없으면 구절 생성
        val originalText = bibleReader.getVerseText(
            translationId = session.translationId,
            bookOrder = session.bookOrder,
            chapterNumber = session.chapterNumber,
            verseNumber = request.verseNumber
        )
        val verse = verseRepository.findByIdOrNull(verseId) ?: createVerse(session, request.verseNumber, originalText)

        // 5. '기존 구절 소요 시간'
        val previousElapsedSeconds = verse.elapsedSeconds

        // 6. '현재 구절 소요 시간' 계산
        val currentElapsedSeconds = calculateDuration(request.startedAt, request.endedAt)

        // 7. '현재 구절 소요 시간' 업데이트
        verse.updateProgress(
            typedText = request.typedText,
            elapsedSeconds = currentElapsedSeconds,
            completed = request.completed
        )

        // 8. 세션에 구절 결과 반영
        session.applyVerseResult(
            previousElapsedSeconds = previousElapsedSeconds,
            currentElapsedSeconds = currentElapsedSeconds,
            typedChars = request.typedText.length,
            verseAccuracy = verse.accuracy,
            completed = verse.completed
        )

        // 9. 구절 저장
        verseRepository.save(verse)

        // 10. 세션 저장
        sessionRepository.save(session)
    }

    @Transactional
    fun resetSession(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ) {
        val session = sessionRepository.findSession(member, translationId, bookOrder, chapterNumber)
            ?: throwError(ErrorType.SESSION_NOT_FOUND)
        sessionRepository.delete(session)
    }

    @Transactional(readOnly = true)
    fun getSessions(
        member: Member,
        translationId: Long?,
        bookOrder: Int?,
        chapterNumber: Int?,
    ): BibleTypingSession {
        return sessionRepository.findSession(
            member = member,
            translationId = translationId,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber,
        ) ?: throwError(ErrorType.SESSION_NOT_FOUND)
    }

    @Transactional(readOnly = true)
    fun getProgress(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): BibleTypingVersesResponse? =
        sessionRepository.findSession(
            member = member,
            translationId = translationId,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber
        )?.let(BibleTypingVersesResponse::from)

    @Transactional(readOnly = true)
    fun getProgress(member: Member): BibleTypingVersesResponse? =
        sessionRepository.findTopByMemberOrderByCreatedAtDesc(member)?.let(BibleTypingVersesResponse::from)

    // ========================
    // private helpers
    // ========================

    private fun parseSessionUid(sessionKey: String): UUID =
        runCatching { UUID.fromString(sessionKey) }
            .getOrElse { throwError(ErrorType.INVALID_SESSION_KEY) }

    private fun getOrCreateSession(
        member: Member,
        request: BibleTypingVerseProgressRequest
    ): BibleTypingSession {
        val sessionUid = parseSessionUid(request.sessionKey)
        return sessionRepository.findBySessionKeyAndMember(sessionUid, member)
            ?: createSessionForVerse(member, request, sessionUid)
    }

    private fun createSessionForVerse(
        member: Member,
        request: BibleTypingVerseProgressRequest,
        sessionUid: UUID
    ): BibleTypingSession {
        val session = BibleTypingSession(
            member = member,
            translationId = request.translationId,
            bookOrder = request.bookOrder,
            chapterNumber = request.chapterNumber,
            sessionKey = sessionUid,
            totalVerses = 0,
            startedAt = Instant.now()
        )
        return sessionRepository.save(session)
    }

    private fun createVerse(
        session: BibleTypingSession,
        verseNumber: Int,
        originalText: String
    ) = BibleTypingVerse.create(
        session = session,
        verseNumber = verseNumber,
        originalText = originalText
    )

    private fun calculateDuration(startedAt: Instant?, endedAt: Instant?): Int {
        if (startedAt == null || endedAt == null) return 0
        val duration = java.time.Duration.between(startedAt, endedAt).toSeconds().toInt()
        return duration.coerceAtLeast(0)
    }

}
