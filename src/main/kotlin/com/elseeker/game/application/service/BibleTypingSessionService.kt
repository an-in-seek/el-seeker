package com.elseeker.game.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionCreateRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionUpdateRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingVerseProgressRequest
import com.elseeker.game.adapter.input.api.response.BibleTypingVersesResponse
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
import java.util.*

@Service
class BibleTypingSessionService(
    private val sessionRepository: BibleTypingSessionRepository,
    private val verseRepository: BibleTypingVerseRepository,
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
    fun updateSession(
        member: Member,
        sessionKey: String,
        request: BibleTypingSessionUpdateRequest
    ) {
        val sessionUid = parseSessionUid(sessionKey)
        val session = sessionRepository.findBySessionKeyAndMember(sessionUid, member) ?: throwError(ErrorType.SESSION_NOT_FOUND)
        if (session.endedAt != null) throwError(ErrorType.SESSION_ALREADY_ENDED)
        session.updateStats(
            totalVerses = request.totalVerses,
            completedVerses = request.completedVerses,
            totalTypedChars = request.totalTypedChars,
            accuracy = request.accuracy,
            cpm = request.cpm,
            endedAt = request.endedAt
        )
    }

    @Transactional
    fun saveVerseProgress(
        member: Member,
        request: BibleTypingVerseProgressRequest
    ) {
        val sessionUid = parseSessionUid(request.sessionKey)
        val session = sessionRepository.findBySessionKeyAndMember(sessionUid, member) ?: createSessionForVerse(member, request, sessionUid)
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
        request: BibleTypingVerseProgressRequest
    ) = BibleTypingVerse.create(
        session = session,
        verseNumber = request.verseNumber,
        originalText = request.originalText
    )

    private fun saveVerseSafely(
        verse: BibleTypingVerse,
        verseId: BibleTypingVerseId
    ) {
        try {
            verseRepository.save(verse)
        } catch (e: DataIntegrityViolationException) {
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
