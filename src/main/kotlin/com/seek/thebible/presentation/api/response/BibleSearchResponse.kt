package com.seek.thebible.presentation.api.response

data class BibleSearchResponse(
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val text: String
)
