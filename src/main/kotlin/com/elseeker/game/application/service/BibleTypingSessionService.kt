package com.elseeker.game.application.service

import com.elseeker.game.adapter.input.api.request.BibleTypingSessionCreateRequest
import com.elseeker.game.adapter.output.jpa.BibleTypingSessionRepository
import com.elseeker.game.domain.model.BibleTypingSession
import com.elseeker.game.domain.model.BibleTypingVerseResult
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class BibleTypingSessionService(
    private val bibleTypingSessionRepository: BibleTypingSessionRepository
) {

    @Transactional
    fun createSession(
        member: Member,
        request: BibleTypingSessionCreateRequest
    ): BibleTypingSession {
        val session = BibleTypingSession(
            member = member,
            translationId = request.translationId,
            bookOrder = request.bookOrder,
            chapterNumber = request.chapterNumber,
            sessionKey = request.sessionKey,
            totalVerses = request.totalVerses,
            completedVerses = request.completedVerses,
            totalTypedChars = request.totalTypedChars,
            accuracy = request.accuracy,
            cpm = request.cpm,
            startedAt = request.startedAt,
            endedAt = request.endedAt
        )
        val verseResults = request.verses.map {
            BibleTypingVerseResult(
                session = session,
                verseNumber = it.verseNumber,
                originalText = it.originalText,
                typedText = it.typedText,
                accuracy = it.accuracy,
                completed = it.completed
            )
        }
        session.addVerseResults(verseResults)
        return bibleTypingSessionRepository.save(session)
    }

    @Transactional(readOnly = true)
    fun getSessions(
        member: Member,
        translationId: Long?,
        bookOrder: Int?,
        chapterNumber: Int?,
        fromDate: LocalDate?,
        toDate: LocalDate?
    ): List<BibleTypingSession> {
        val fromDateTime = fromDate?.atStartOfDay()
        val toDateTime = toDate?.atTime(23, 59, 59)
        return bibleTypingSessionRepository.findSessions(
            member = member,
            translationId = translationId,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber,
            fromDate = fromDateTime,
            toDate = toDateTime
        )
    }
}
