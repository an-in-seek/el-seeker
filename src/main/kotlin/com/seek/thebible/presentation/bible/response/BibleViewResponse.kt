package com.seek.thebible.presentation.bible.response

import com.seek.thebible.domain.bible.model.BibleBook
import com.seek.thebible.domain.bible.model.BibleTranslationType
import com.seek.thebible.domain.bible.result.BibleResult

object BibleViewResponse {

    data class Translation(
        val translationId: Long,
        val translationType: BibleTranslationType,
        val translationName: String
    ) {
        companion object {
            fun from(result: BibleResult.Translation) =
                Translation(
                    translationId = result.translationId,
                    translationType = result.translationType,
                    translationName = result.translationName
                )
        }
    }

    data class Chapter(
        val book: BibleResult.BookDetail
    ) {
        companion object {
            fun from(book: BibleBook) =
                Chapter(
                    book = book.let(BibleResult.BookDetail::from)
                )
        }
    }

}