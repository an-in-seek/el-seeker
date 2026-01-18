package com.elseeker.game.adapter.input.api.dto

import java.time.LocalDateTime

object BibleTypingSessionDto {

    data class CreateRequest(
        val sessionKey: String,
        val translationId: Long,
        val bookOrder: Int,
        val chapterNumber: Int,
        val startedAt: LocalDateTime,
        val endedAt: LocalDateTime,
        val totalVerses: Int,
        val completedVerses: Int,
        val totalTypedChars: Int,
        val accuracy: Double,
        val cpm: Double,
        val verses: List<VerseResult>
    )

    data class VerseResult(
        val verseNumber: Int,
        val originalText: String,
        val typedText: String,
        val accuracy: Double,
        val completed: Boolean
    )

    data class Response(
        val sessionId: Long,
        val createdAt: LocalDateTime
    )

    data class SummaryResponse(
        val sessionId: Long,
        val translationId: Long,
        val bookOrder: Int,
        val chapterNumber: Int,
        val totalVerses: Int,
        val completedVerses: Int,
        val accuracy: Double,
        val cpm: Double,
        val createdAt: LocalDateTime
    )
}
