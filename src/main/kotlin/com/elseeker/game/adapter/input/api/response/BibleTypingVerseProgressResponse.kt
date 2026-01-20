package com.elseeker.game.adapter.input.api.response

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
}
