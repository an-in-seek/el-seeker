package com.elseeker.bible.application.bible.service

import com.elseeker.bible.application.bible.component.BibleReader
import com.elseeker.bible.domain.bible.DirectionType
import com.elseeker.bible.domain.bible.result.BibleResult
import com.elseeker.bible.presentation.api.BibleApiResponse
import com.elseeker.bible.presentation.api.response.BibleSearchSliceResponse
import org.springframework.stereotype.Service

@Service
class BibleService(
    private val bibleReader: BibleReader
) {

    fun getTranslations(): List<BibleResult.Translation> =
        bibleReader.getTranslations()

    fun getBook(translationId: Long, bookOrder: Int): BibleApiResponse.BookDetail? =
        bibleReader.getBook(translationId, bookOrder)

    fun getBooks(translationId: Long): List<BibleResult.Book> =
        bibleReader.getBooks(translationId)

    fun getChapters(translationId: Long, bookOrder: Int): BibleApiResponse.Chapters =
        bibleReader.getChapters(translationId, bookOrder)

    fun getChapterVerses(translationId: Long, bookOrder: Int, chapterNumber: Int): BibleApiResponse.Verses =
        bibleReader.getChapterVerses(translationId, bookOrder, chapterNumber)

    fun getAdjacentChapterVerses(translationId: Long, bookOrder: Int, chapterNumber: Int, direction: DirectionType): BibleApiResponse.Verses =
        bibleReader.getAdjacentChapterVerses(translationId, bookOrder, chapterNumber, direction)

    fun searchBibleVersesSlice(
        translationId: Long,
        keyword: String,
        page: Int,
        size: Int
    ): BibleSearchSliceResponse =
        bibleReader.searchBibleVersesSlice(translationId, keyword, page, size)
}
