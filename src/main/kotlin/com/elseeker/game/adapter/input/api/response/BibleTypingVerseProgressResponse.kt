package com.elseeker.game.adapter.input.api.response

import java.time.LocalDateTime

data class BibleTypingVerseProgressResponse(
    val sessionKey: String,
    val createdAt: LocalDateTime,
    val verses: List<VerseProgress>
) {
    data class VerseProgress(
        val verseNumber: Int,
        val typedText: String,
        val completed: Boolean,
        val createdAt: LocalDateTime
    )
}
