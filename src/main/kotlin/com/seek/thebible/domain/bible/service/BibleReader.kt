package com.seek.thebible.domain.bible.service

import com.seek.thebible.domain.BibleServiceException
import com.seek.thebible.domain.DirectionType
import com.seek.thebible.domain.ErrorType
import com.seek.thebible.domain.bible.dto.BookResult
import com.seek.thebible.domain.bible.dto.ChapterView
import com.seek.thebible.domain.bible.dto.TranslationResult
import com.seek.thebible.domain.bible.dto.VerseViewResult
import com.seek.thebible.domain.bible.model.BibleTranslationType
import com.seek.thebible.domain.bible.model.BibleVerse
import com.seek.thebible.infrastructure.persistence.bible.BibleBookRepository
import com.seek.thebible.infrastructure.persistence.bible.BibleChapterRepository
import com.seek.thebible.infrastructure.persistence.bible.BibleTranslationRepository
import com.seek.thebible.infrastructure.persistence.bible.BibleVerseRepository
import org.springframework.stereotype.Service

@Service
class BibleReader(
    private val bibleTranslationRepository: BibleTranslationRepository,
    private val bibleBookRepository: BibleBookRepository,
    private val bibleChapterRepository: BibleChapterRepository,
    private val bibleVerseRepository: BibleVerseRepository
) {

    fun getTranslations(): List<TranslationResult> =
        bibleTranslationRepository.findAllByTranslationTypeInOrderByTranslationOrder(
            setOf(
                BibleTranslationType.KRV,
                BibleTranslationType.NKRV,
                BibleTranslationType.NIV,
                BibleTranslationType.ESV,
                BibleTranslationType.KJV
            )
        ).map(TranslationResult::from)

    fun getBooks(translationId: Long): List<BookResult> =
        bibleBookRepository.findByTranslationId(translationId).map(BookResult::from)

    fun getChapterView(bookId: Long): ChapterView =
        bibleBookRepository.findByIdWithChapters(bookId)
            ?.let(ChapterView::from)
            ?: throw BibleServiceException(ErrorType.BOOK_NOT_FOUND, "bookId=$bookId")

    fun getVerseView(translationId: Long, bookId: Long, chapterNumber: Int): VerseViewResult {
        val translation = bibleTranslationRepository.findByIdWithBooks(translationId)
            ?: throw BibleServiceException(ErrorType.TRANSLATION_NOT_FOUND)

        val books = translation.books
        val book = books.firstOrNull { it.id == bookId }
            ?: throw BibleServiceException(ErrorType.BOOK_NOT_FOUND)

        val chapter = bibleChapterRepository.findByBookIdAndChapterNumberWithVerses(bookId, chapterNumber)
            ?: throw BibleServiceException(ErrorType.CHAPTER_NOT_FOUND)

        val totalChapterCount = bibleChapterRepository.countByBookId(bookId)

        return VerseViewResult.of(
            books = books,
            currentBook = book,
            totalChapterCount = totalChapterCount,
            chapter = chapter
        )
    }

    fun navigateChapter(
        translationId: Long,
        bookId: Long,
        chapterNumber: Int,
        direction: DirectionType
    ): VerseViewResult {
        val translation = bibleTranslationRepository.findByIdWithBooks(translationId)
            ?: throw BibleServiceException(ErrorType.TRANSLATION_NOT_FOUND)

        val books = translation.books.sortedBy { it.bookOrder }
        val currentBookIndex = books.indexOfFirst { it.id == bookId }
        val currentBook = books.getOrNull(currentBookIndex)
            ?: throw BibleServiceException(ErrorType.BOOK_NOT_FOUND)

        val currentChapterCount = currentBook.chapters.size

        var targetBook = currentBook
        var targetChapterNumber = chapterNumber

        when (direction) {
            DirectionType.NEXT -> {
                if (chapterNumber < currentChapterCount) {
                    targetChapterNumber += 1
                } else if (currentBookIndex < books.lastIndex) {
                    targetBook = books[currentBookIndex + 1]
                    targetChapterNumber = 1
                }
            }

            DirectionType.PREV -> {
                if (chapterNumber > 1) {
                    targetChapterNumber -= 1
                } else if (currentBookIndex > 0) {
                    targetBook = books[currentBookIndex - 1]
                    targetChapterNumber = targetBook.chapters.maxOfOrNull { it.chapterNumber }
                        ?: throw BibleServiceException(ErrorType.CHAPTER_NOT_FOUND)
                }
            }
        }

        val targetBookId = targetBook.id ?: throw IllegalStateException("Book has no ID")
        val chapter = bibleChapterRepository.findByBookIdAndChapterNumberWithVerses(targetBookId, targetChapterNumber)
            ?: throw BibleServiceException(ErrorType.CHAPTER_NOT_FOUND)

        val totalChapterCount = bibleChapterRepository.countByBookId(targetBook.id!!)

        return VerseViewResult.of(
            books = books,
            currentBook = targetBook,
            totalChapterCount = totalChapterCount,
            chapter = chapter
        )
    }

    fun searchBibleVerses(keyword: String): List<BibleVerse> {
        if (keyword.isBlank()) throw BibleServiceException(ErrorType.INVALID_PARAMETER, "keyword is blank")
        return try {
            bibleVerseRepository.findByTextContaining(keyword)
        } catch (e: Exception) {
            throw BibleServiceException(ErrorType.SEARCH_ERROR, "keyword=$keyword", e.message ?: "Unknown error")
        }
    }
}
