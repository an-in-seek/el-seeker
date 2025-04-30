package com.seek.thebible.presentation.bible.dto

import com.seek.thebible.domain.bible.dto.BibleSearchResult

data class BibleSearchResponse(
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val text: String
) {
    companion object {
        fun from(result: BibleSearchResult) =
            BibleSearchResponse(
                bookName = result.bookName,
                chapterNumber = result.chapterNumber,
                verseNumber = result.verseNumber,
                text = result.text,
            )
    }
}
