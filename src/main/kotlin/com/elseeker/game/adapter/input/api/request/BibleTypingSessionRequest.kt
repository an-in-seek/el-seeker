package com.elseeker.game.adapter.input.api.request

import java.time.LocalDateTime

data class BibleTypingSessionCreateRequest(
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
    val verses: List<BibleTypingSessionVerseRequest>
)

data class BibleTypingSessionVerseRequest(
    val verseNumber: Int,
    val originalText: String,
    val typedText: String,
    val accuracy: Double,
    val completed: Boolean
)
