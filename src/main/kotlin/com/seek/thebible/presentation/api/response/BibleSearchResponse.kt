package com.seek.thebible.presentation.api.response

data class BibleSearchResponse(
    val bookId: Long,
    val bookName: String,
    val chapterId: Long,
    val chapterNumber: Int,
    val verseId: Long,
    val verseNumber: Int,
    val text: String
)
