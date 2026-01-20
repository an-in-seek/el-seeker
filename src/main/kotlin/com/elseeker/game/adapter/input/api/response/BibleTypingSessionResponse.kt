package com.elseeker.game.adapter.input.api.response

import java.time.Instant

data class BibleTypingSessionResponse(
    val sessionId: Long,
    val createdAt: Instant
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
    val startedAt: Instant,
    val endedAt: Instant,
    val createdAt: Instant
)
