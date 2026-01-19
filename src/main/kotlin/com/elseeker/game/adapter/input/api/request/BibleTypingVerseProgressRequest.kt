package com.elseeker.game.adapter.input.api.request

data class BibleTypingVerseProgressRequest(
    val sessionKey: String,
    val translationId: Long,
    val bookOrder: Int,
    val chapterNumber: Int,
    val verseNumber: Int,
    val originalText: String,
    val typedText: String,
    val accuracy: Double,
    val cpm: Double,
    val elapsedSeconds: Int,
    val completed: Boolean
)
