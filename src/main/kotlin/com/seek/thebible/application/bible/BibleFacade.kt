package com.seek.thebible.application.bible

import com.seek.thebible.domain.DirectionType
import com.seek.thebible.domain.bible.dto.*
import com.seek.thebible.domain.bible.service.BibleReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BibleFacade(
    private val bibleReader: BibleReader
) {

    fun getTranslations(): List<TranslationResult> = bibleReader.getTranslations()

    fun getBooks(translationId: Long): List<BookResult> = bibleReader.getBooks(translationId)

    fun getChapterView(bookId: Long): ChapterView = bibleReader.getChapterView(bookId)

    fun getVerseView(translationId: Long, bookId: Long, chapterNumber: Int): VerseViewResult =
        bibleReader.getVerseView(translationId, bookId, chapterNumber)

    fun navigate(translationId: Long, bookId: Long, chapterNumber: Int, direction: DirectionType): VerseViewResult =
        bibleReader.navigateChapter(translationId, bookId, chapterNumber, direction)

    fun searchBibleVerses(keyword: String): List<SearchVerseResult> =
        bibleReader.searchBibleVerses(keyword).map { verse ->
            SearchVerseResult(
                verseId = verse.id!!,
                verseNumber = verse.verseNumber,
                text = verse.text
            )
        }
}
