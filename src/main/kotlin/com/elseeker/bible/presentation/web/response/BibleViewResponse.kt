package com.elseeker.bible.presentation.web.response

import com.elseeker.bible.domain.bible.model.BibleBook
import com.elseeker.bible.domain.bible.model.BibleTranslationType
import com.elseeker.bible.domain.bible.result.BibleResult

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