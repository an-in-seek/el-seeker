package com.elseeker.game.adapter.input.api.response

import java.time.LocalDateTime

data class BibleTypingSessionResponse(
    val sessionId: Long,
    val createdAt: LocalDateTime
)

data class BibleTypingSessionSummaryResponse(
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
