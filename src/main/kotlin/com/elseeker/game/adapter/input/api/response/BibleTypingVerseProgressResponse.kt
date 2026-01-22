package com.elseeker.game.adapter.input.api.response

import com.elseeker.game.domain.model.BibleTypingSession
import java.time.Instant

data class BibleTypingVerseProgressResponse(
    val sessionKey: String,
    val translationId: Long,
    val bookOrder: Int,
    val chapterNumber: Int,
    val createdAt: Instant,
    val verses: List<VerseProgress>
) {
    data class VerseProgress(
        val verseNumber: Int,
        val typedText: String,
        val accuracy: Double,
        val cpm: Double,
        val elapsedSeconds: Int,
        val completed: Boolean,
        val createdAt: Instant
    )

    companion object {
        fun from(session: BibleTypingSession): BibleTypingVerseProgressResponse {
            val verses = session.verses
                .asSequence()
                .map {
                    VerseProgress(
                        verseNumber = it.verseNumber,
                        typedText = it.typedText,
                        accuracy = it.accuracy,
                        cpm = it.cpm,
                        elapsedSeconds = it.elapsedSeconds,
                        completed = it.completed,
                        createdAt = it.createdAt
                    )
                }
                .sortedBy { it.verseNumber }
                .toList()
            return BibleTypingVerseProgressResponse(
                sessionKey = session.sessionUid.toString(),
                translationId = session.translationId,
                bookOrder = session.bookOrder,
                chapterNumber = session.chapterNumber,
                createdAt = session.createdAt,
                verses = verses
            )
        }
    }
}
