package com.seek.thebible.presentation.bible.dto

import com.seek.thebible.domain.bible.dto.VerseViewResult

data class VerseViewResponse(
    val book: VerseViewResult.Book,
    val hasPrev: Boolean,
    val hasNext: Boolean,
    val isFirst: Boolean,
    val isLast: Boolean?,
) {
    companion object {
        fun from(result: VerseViewResult) = VerseViewResponse(
            book = result.book,
            hasPrev = result.hasPrev,
            hasNext = result.hasNext,
            isFirst = result.isFirst,
            isLast = result.isLast
        )
    }
}
