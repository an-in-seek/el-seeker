package com.seek.thebible.domain.bible.service

import com.seek.thebible.domain.BibleServiceException
import com.seek.thebible.domain.DirectionType
import com.seek.thebible.domain.ErrorType
import com.seek.thebible.domain.bible.model.BibleTranslationType
import com.seek.thebible.domain.bible.result.BibleResult
import com.seek.thebible.infrastructure.persistence.bible.BibleBookRepository
import com.seek.thebible.infrastructure.persistence.bible.BibleChapterRepository
import com.seek.thebible.infrastructure.persistence.bible.BibleTranslationRepository
import com.seek.thebible.infrastructure.persistence.bible.BibleVerseRepository
import com.seek.thebible.presentation.api.BibleApiResponse
import com.seek.thebible.presentation.api.response.BibleSearchResponse
import com.seek.thebible.presentation.web.response.BibleViewResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class BibleReader(
    private val bibleTranslationRepository: BibleTranslationRepository,
    private val bibleBookRepository: BibleBookRepository,
    private val bibleChapterRepository: BibleChapterRepository,
    private val bibleVerseRepository: BibleVerseRepository
) {

    fun getTranslations(): List<BibleResult.Translation> =
        bibleTranslationRepository.findAllByTranslationTypeInOrderByTranslationOrder(
            setOf(BibleTranslationType.KRV, BibleTranslationType.KJV)
        ).map(BibleResult.Translation::from)

    fun getBooks(translationId: Long): List<BibleResult.Book> =
        bibleBookRepository.findByTranslationId(translationId).map(BibleResult.Book::from)

    fun getChapterView(bookId: Long): BibleViewResponse.Chapter =
        bibleBookRepository.findByIdWithChapters(bookId)
            ?.let(BibleViewResponse.Chapter::from)
            ?: throw BibleServiceException(ErrorType.BOOK_NOT_FOUND, "bookId=$bookId")

    fun getVerseView(translationId: Long, bookId: Long, chapterNumber: Int): BibleApiResponse.Verse {
        val translation = bibleTranslationRepository.findByIdWithBooks(translationId)
            ?: throw BibleServiceException(ErrorType.TRANSLATION_NOT_FOUND)

        val books = translation.books
        val book = books.firstOrNull { it.id == bookId }
            ?: throw BibleServiceException(ErrorType.BOOK_NOT_FOUND)

        val chapter = bibleChapterRepository.findByBookIdAndChapterNumberWithVerses(bookId, chapterNumber)
            ?: throw BibleServiceException(ErrorType.CHAPTER_NOT_FOUND)

        val totalChapterCount = bibleChapterRepository.countByBookId(bookId)

        return BibleApiResponse.Verse.of(
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
    ): BibleApiResponse.Verse {
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

        return BibleApiResponse.Verse.of(
            books = books,
            currentBook = targetBook,
            totalChapterCount = totalChapterCount,
            chapter = chapter
        )
    }

    fun searchBibleVerses(
        translationId: Long,
        keyword: String
    ): List<BibleSearchResponse> {
        if (keyword.isBlank()) throw BibleServiceException(ErrorType.INVALID_PARAMETER, "keyword is blank")
        return try {
            bibleVerseRepository.searchByTranslationAndText(translationId, keyword)
        } catch (e: Exception) {
            throw BibleServiceException(ErrorType.SEARCH_ERROR, "keyword=$keyword", e.message ?: "Unknown error")
        }
    }
}
