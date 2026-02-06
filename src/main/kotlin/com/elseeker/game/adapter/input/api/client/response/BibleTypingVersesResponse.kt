package com.elseeker.game.adapter.input.api.client.response

import com.elseeker.game.domain.model.BibleTypingSession
import java.time.Instant

data class BibleTypingVersesResponse(
    val sessionKey: String,
    val translationId: Long,
    val bookOrder: Int,
    val chapterNumber: Int,
    val createdAt: Instant,
    val accuracy: Int,
    val cpm: Int,
    val totalElapsedSeconds: Int,
    val verses: List<VerseProgress>
) {
    data class VerseProgress(
        val verseNumber: Int,
        val typedText: String,
        val accuracy: Int,
        val cpm: Int,
        val elapsedSeconds: Int,
        val completed: Boolean,
        val createdAt: Instant
    )

    companion object {
        fun from(session: BibleTypingSession): BibleTypingVersesResponse {
            val verses = session.verses
                .asSequence()
                .map {
                    VerseProgress(
                        verseNumber = it.verseNumber,
                        typedText = it.typedText,
                        accuracy = it.accuracy.toInt(),
                        cpm = it.cpm.toInt(),
                        elapsedSeconds = it.elapsedSeconds,
                        completed = it.completed,
                        createdAt = it.createdAt
                    )
                }
                .sortedBy { it.verseNumber }
                .toList()
            return BibleTypingVersesResponse(
                sessionKey = session.sessionKey.toString(),
                translationId = session.translationId,
                bookOrder = session.bookOrder,
                chapterNumber = session.chapterNumber,
                accuracy = session.accuracy.toInt(),
                cpm = session.cpm.toInt(),
                totalElapsedSeconds = session.totalElapsedSeconds,
                verses = verses,
                createdAt = session.createdAt,
            )
        }
    }
}
