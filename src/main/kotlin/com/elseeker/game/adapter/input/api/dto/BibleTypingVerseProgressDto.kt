package com.elseeker.game.adapter.input.api.dto

data class BibleTypingVerseProgressRequest(
    val sessionKey: String,
    val translationId: Long,
    val bookOrder: Int,
    val chapterNumber: Int,
    val verseNumber: Int,
    val originalText: String,
    val typedText: String,
    val accuracy: Double,
    val completed: Boolean
)

data class BibleTypingVerseProgressResponse(
    val sessionKey: String,
    val createdAt: java.time.LocalDateTime,
    val verses: List<VerseProgress>
) {
    data class VerseProgress(
        val verseNumber: Int,
        val typedText: String,
        val completed: Boolean,
        val createdAt: java.time.LocalDateTime
    )
}
