package com.seek.thebible.application.bible

import com.seek.thebible.domain.bible.DirectionType
import com.seek.thebible.domain.bible.result.BibleResult
import com.seek.thebible.domain.bible.service.BibleReader
import com.seek.thebible.presentation.api.BibleApiResponse
import com.seek.thebible.presentation.api.response.BibleSearchResponse
import com.seek.thebible.presentation.web.response.BibleViewResponse
import org.springframework.stereotype.Service

@Service
class BibleFacade(
    private val bibleReader: BibleReader
) {

    fun getTranslations(): List<BibleResult.Translation> =
        bibleReader.getTranslations()

    fun getBooks(translationId: Long): List<BibleResult.Book> =
        bibleReader.getBooks(translationId)

    fun getChapterView(translationId: Long, bookOrder: Int): BibleViewResponse.Chapter =
        bibleReader.getChapterView(translationId, bookOrder)

    fun getChapterVerses(translationId: Long, bookOrder: Int, chapterNumber: Int): BibleApiResponse.Verse =
        bibleReader.getChapterVerses(translationId, bookOrder, chapterNumber)

    fun getAdjacentChapterVerses(translationId: Long, bookOrder: Int, chapterNumber: Int, direction: DirectionType): BibleApiResponse.Verse =
        bibleReader.getAdjacentChapterVerses(translationId, bookOrder, chapterNumber, direction)

    fun searchBibleVerses(translationId: Long, keyword: String): List<BibleSearchResponse> =
        bibleReader.searchBibleVerses(translationId, keyword);
}
