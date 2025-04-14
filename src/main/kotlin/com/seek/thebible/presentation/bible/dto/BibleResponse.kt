package com.seek.thebible.presentation.bible.dto

import com.seek.thebible.domain.bible.dto.SearchVerseResult

data class SearchVerseResponse(
    val verseId: Long,
    val verseNumber: Int,
    val text: String
) {
    companion object {
        fun from(result: SearchVerseResult) =
            SearchVerseResponse(
                verseId = result.verseId,
                verseNumber = result.verseNumber,
                text = result.text
            )
    }
}
