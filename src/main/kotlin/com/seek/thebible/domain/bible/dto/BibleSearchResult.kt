package com.seek.thebible.domain.bible.dto

data class BibleSearchResult(
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val text: String
)
