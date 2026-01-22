package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionCreateRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingVerseProgressRequest
import com.elseeker.game.adapter.input.api.response.BibleTypingVerseProgressResponse
import com.elseeker.game.adapter.output.jpa.BibleTypingSessionRepository
import com.elseeker.game.adapter.output.jpa.BibleTypingVerseRepository
import com.elseeker.game.domain.model.BibleTypingSession
import com.elseeker.game.domain.model.BibleTypingVerse
import com.elseeker.game.domain.model.BibleTypingVerseId
import com.elseeker.member.domain.model.Member
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

@Service
class BibleTypingSessionService(
    private val sessionRepository: BibleTypingSessionRepository,
    private val verseRepository: BibleTypingVerseRepository
) {

    /**
     * 세션 생성 (서버 주도, 단일 진입점)
     */
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
        ).let {
            sessionRepository.save(it)
        }
    }

    /**
     * 절 진행 상태 저장 / 갱신
     */
    @Transactional
    fun saveVerseProgress(
        member: Member,
        request: BibleTypingVerseProgressRequest
    ) {
        val sessionUid = parseSessionUid(request.sessionKey)
        val session = sessionRepository.findBySessionUidAndMember(sessionUid, member)
            ?: BibleTypingSession.create(
                member = member,
                translationId = request.translationId,
                bookOrder = request.bookOrder,
                chapterNumber = request.chapterNumber,
            ).let {
                sessionRepository.save(it)
            }
        if (session.endedAt != null) throwError(ErrorType.SESSION_ALREADY_ENDED)
        val verseId = BibleTypingVerseId.of(sessionId = session.id!!, verseNumber = request.verseNumber)
        val verse = verseRepository.findByIdOrNull(verseId) ?: createVerse(session, request)
        verse.updateProgress(
            typedText = request.typedText,
            accuracy = request.accuracy,
            cpm = request.cpm,
            elapsedSeconds = request.elapsedSeconds,
            completed = request.completed
        )
        saveVerseSafely(verse, verseId)
    }

    /**
     * 세션 목록 조회
     */
    @Transactional(readOnly = true)
    fun getSessions(
        member: Member,
        translationId: Long?,
        bookOrder: Int?,
        chapterNumber: Int?,
        fromDate: LocalDate?,
        toDate: LocalDate?
    ): List<BibleTypingSession> {
        val zone = ZoneOffset.UTC
        val from = fromDate?.atStartOfDay(zone)?.toInstant() ?: Instant.EPOCH
        val to = toDate?.atTime(23, 59, 59)?.atZone(zone)?.toInstant() ?: Instant.parse("9999-12-31T23:59:59Z")
        return sessionRepository.findSessions(
            member = member,
            translationId = translationId,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber,
            fromDate = from,
            toDate = to
        )
    }

    @Transactional(readOnly = true)
    fun getLatestProgress(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): BibleTypingVerseProgressResponse? =
        sessionRepository.findLatestByScope(
            member = member,
            translationId = translationId,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber
        ).firstOrNull()?.let(BibleTypingVerseProgressResponse::from)

    @Transactional(readOnly = true)
    fun getLatestProgress(member: Member): BibleTypingVerseProgressResponse? =
        sessionRepository.findLatestByMember(member)
            .firstOrNull()
            ?.let(BibleTypingVerseProgressResponse::from)

    // ========================
    // private helpers
    // ========================

    private fun parseSessionUid(sessionKey: String): UUID =
        runCatching { UUID.fromString(sessionKey) }
            .getOrElse { throwError(ErrorType.INVALID_SESSION_KEY) }

    private fun createVerse(
        session: BibleTypingSession,
        request: BibleTypingVerseProgressRequest
    ) = BibleTypingVerse.create(
        session = session,
        verseNumber = request.verseNumber,
        originalText = request.originalText,
    )

    /**
     * PK 충돌(insert race)까지 고려한 안전 저장
     */
    private fun saveVerseSafely(
        verse: BibleTypingVerse,
        verseId: BibleTypingVerseId
    ) {
        try {
            verseRepository.save(verse)
        } catch (e: DataIntegrityViolationException) {
            // 동시 insert 충돌 → 재조회 후 update
            val existing = verseRepository.findByIdOrNull(verseId) ?: throw e
            existing.updateProgress(
                typedText = verse.typedText,
                accuracy = verse.accuracy,
                cpm = verse.cpm,
                elapsedSeconds = verse.elapsedSeconds,
                completed = verse.completed
            )
        }
    }

}
