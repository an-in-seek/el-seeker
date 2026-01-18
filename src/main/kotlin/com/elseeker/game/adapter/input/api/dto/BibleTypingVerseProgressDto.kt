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
