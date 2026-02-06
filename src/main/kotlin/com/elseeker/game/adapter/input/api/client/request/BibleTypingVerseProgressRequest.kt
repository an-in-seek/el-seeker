package com.elseeker.game.adapter.input.api.client.request

import java.time.Instant

data class BibleTypingVerseProgressRequest(
    val sessionKey: String,
    val translationId: Long,
    val bookOrder: Int,
    val chapterNumber: Int,
    val verseNumber: Int,
    val typedText: String,
    val startedAt: Instant?,
    val endedAt: Instant?,
    val completed: Boolean
)
