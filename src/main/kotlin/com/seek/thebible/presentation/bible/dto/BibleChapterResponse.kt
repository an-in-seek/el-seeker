package com.seek.thebible.presentation.bible.dto

import com.seek.thebible.domain.bible.dto.BookDetailResult
import com.seek.thebible.domain.bible.dto.ChapterView

data class ChapterViewResponse(
    val book: BookDetailResult
) {
    companion object {
        fun from(result: ChapterView) =
            ChapterViewResponse(book = result.book)
    }
}
