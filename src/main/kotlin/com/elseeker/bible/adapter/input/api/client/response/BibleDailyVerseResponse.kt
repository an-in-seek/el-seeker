package com.elseeker.bible.adapter.input.api.client.response

import com.elseeker.bible.domain.vo.BibleTranslationType

data class BibleDailyVerseResponse(
    val translationType: BibleTranslationType,
    val translationName: String,
    val bookOrder: Int,
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val text: String
) {
    companion object {
        fun from(translationType: BibleTranslationType, verse: BibleSearchResponse) =
            BibleDailyVerseResponse(
                translationType = translationType,
                translationName = translationType.displayName,
                bookOrder = verse.bookOrder,
                bookName = verse.bookName,
                chapterNumber = verse.chapterNumber,
                verseNumber = verse.verseNumber,
                text = verse.text
            )
    }
}
