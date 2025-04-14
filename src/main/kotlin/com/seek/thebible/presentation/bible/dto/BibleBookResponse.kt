package com.seek.thebible.presentation.bible.dto

import com.seek.thebible.domain.bible.dto.BookResult
import com.seek.thebible.domain.bible.model.BibleTestamentType

data class BookResponse(
    val bookId: Long,
    val bookName: String,
    val abbreviation: String,
    val testamentType: BibleTestamentType,
    val chapterCount: Int,
) {
    companion object {
        fun from(result: BookResult) =
            BookResponse(
                bookId = result.bookId,
                bookName = result.bookName,
                abbreviation = result.abbreviation,
                testamentType = result.testamentType,
                chapterCount = result.chapterCount
            )
    }
}
