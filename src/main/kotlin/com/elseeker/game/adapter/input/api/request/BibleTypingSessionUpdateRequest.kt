package com.elseeker.game.adapter.input.api.request

import java.time.Instant

data class BibleTypingSessionUpdateRequest(
    val totalVerses: Int,
    val completedVerses: Int,
    val totalTypedChars: Int,
    val accuracy: Double,
    val cpm: Double,
    val endedAt: Instant
)
