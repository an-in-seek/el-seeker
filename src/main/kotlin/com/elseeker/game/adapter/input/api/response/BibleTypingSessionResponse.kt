package com.elseeker.game.adapter.input.api.response

import java.time.LocalDateTime

data class BibleTypingSessionResponse(
    val sessionId: Long,
    val createdAt: LocalDateTime
)

data class BibleTypingSessionSummaryResponse(
    val sessionId: Long,
    val sessionKey: String,
    val translationId: Long,
    val bookOrder: Int,
    val chapterNumber: Int,
    val totalVerses: Int,
    val completedVerses: Int,
    val accuracy: Double,
    val cpm: Double,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
    val createdAt: LocalDateTime
)
