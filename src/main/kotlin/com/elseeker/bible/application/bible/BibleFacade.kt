package com.elseeker.bible.application.bible

import com.elseeker.bible.domain.bible.DirectionType
import com.elseeker.bible.domain.bible.result.BibleResult
import com.elseeker.bible.domain.bible.service.BibleReader
import com.elseeker.bible.presentation.api.BibleApiResponse
import com.elseeker.bible.presentation.api.response.BibleSearchResponse
import org.springframework.stereotype.Service

@Service
class BibleFacade(
    private val bibleReader: BibleReader
) {

    fun getTranslations(): List<BibleResult.Translation> =
        bibleReader.getTranslations()

    fun getBooks(translationId: Long): List<BibleResult.Book> =
        bibleReader.getBooks(translationId)

    fun getChapterView(translationId: Long, bookOrder: Int): BibleApiResponse.Chapters =
        bibleReader.getChapters(translationId, bookOrder)

    fun getChapterVerses(translationId: Long, bookOrder: Int, chapterNumber: Int): BibleApiResponse.Verses =
        bibleReader.getChapterVerses(translationId, bookOrder, chapterNumber)

    fun getAdjacentChapterVerses(translationId: Long, bookOrder: Int, chapterNumber: Int, direction: DirectionType): BibleApiResponse.Verses =
        bibleReader.getAdjacentChapterVerses(translationId, bookOrder, chapterNumber, direction)

    fun searchBibleVerses(translationId: Long, keyword: String): List<BibleSearchResponse> =
        bibleReader.searchBibleVerses(translationId, keyword);
}
