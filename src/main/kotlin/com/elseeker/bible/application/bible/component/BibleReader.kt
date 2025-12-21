package com.elseeker.bible.application.bible.component

import com.elseeker.bible.domain.ErrorType
import com.elseeker.bible.domain.ServiceException
import com.elseeker.bible.domain.bible.DirectionType
import com.elseeker.bible.domain.bible.model.BibleTranslationType
import com.elseeker.bible.domain.bible.result.BibleResult
import com.elseeker.bible.infrastructure.persistence.jpa.BibleBookRepository
import com.elseeker.bible.infrastructure.persistence.jpa.BibleChapterRepository
import com.elseeker.bible.infrastructure.persistence.jpa.BibleTranslationRepository
import com.elseeker.bible.infrastructure.persistence.jpa.BibleVerseRepository
import com.elseeker.bible.presentation.api.BibleApiResponse
import com.elseeker.bible.presentation.api.response.BibleSearchResponse
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
        bibleTranslationRepository.findAllByTranslationTypeInOrderByTranslationOrder(setOf(BibleTranslationType.KRV, BibleTranslationType.KJV))
            .map(BibleResult.Translation::from)

    fun getBook(translationId: Long, bookOrder: Int): BibleApiResponse.BookDetail? =
        bibleBookRepository.findByTranslationAndBook(translationId, bookOrder)
            ?.let(BibleApiResponse.BookDetail::from)

    fun getBooks(translationId: Long): List<BibleResult.Book> =
        bibleBookRepository.findByTranslationId(translationId)
            .map(BibleResult.Book::from)

    fun getChapters(translationId: Long, bookOrder: Int): BibleApiResponse.Chapters =
        bibleBookRepository.findByTranslationAndBook(translationId, bookOrder)
            ?.let(BibleApiResponse.Chapters::from)
            ?: throw ServiceException(ErrorType.BOOK_NOT_FOUND)

    fun getChapterVerses(
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): BibleApiResponse.Verses {
        val translation = bibleTranslationRepository.findByIdWithBooks(translationId)
            ?: throw ServiceException(ErrorType.TRANSLATION_NOT_FOUND)

        val books = translation.books.sortedBy { it.bookOrder }
        val book = books.firstOrNull { it.bookOrder == bookOrder } ?: throw ServiceException(ErrorType.BOOK_NOT_FOUND)
        val bookId = book.id!!

        val chapter = bibleChapterRepository.findByBookAndChapter(bookId, chapterNumber)
            ?: throw ServiceException(ErrorType.CHAPTER_NOT_FOUND)

        val totalChapterCount = bibleChapterRepository.findMaxChapterNumberByBookId(bookId)
            ?: throw ServiceException(ErrorType.CHAPTER_NOT_FOUND)

        return BibleApiResponse.Verses.of(
            books = books,
            currentBook = book,
            totalChapterCount = totalChapterCount,
            chapter = chapter
        )
    }

    fun getAdjacentChapterVerses(
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        direction: DirectionType
    ): BibleApiResponse.Verses {
        val translation = bibleTranslationRepository.findByIdWithBooks(translationId)
            ?: throw ServiceException(ErrorType.TRANSLATION_NOT_FOUND)

        val books = translation.books.sortedBy { it.bookOrder }
        val currentBookIndex = books.indexOfFirst { it.bookOrder == bookOrder }
        val currentBook = books.getOrNull(currentBookIndex) ?: throw ServiceException(ErrorType.BOOK_NOT_FOUND)
        val currentBookId = currentBook.id ?: throw IllegalStateException("Book has no ID")
        val currentMaxChapterNumber = bibleChapterRepository.findMaxChapterNumberByBookId(currentBookId)
            ?: throw ServiceException(ErrorType.CHAPTER_NOT_FOUND)

        var targetBook = currentBook
        var targetChapterNumber = chapterNumber
        var targetMaxChapterNumber = currentMaxChapterNumber

        when (direction) {
            DirectionType.NEXT -> {
                if (chapterNumber < currentMaxChapterNumber) {
                    targetChapterNumber += 1
                } else if (currentBookIndex < books.lastIndex) {
                    targetBook = books[currentBookIndex + 1]
                    targetChapterNumber = 1
                    val nextBookId = targetBook.id ?: throw IllegalStateException("Book has no ID")
                    targetMaxChapterNumber = bibleChapterRepository.findMaxChapterNumberByBookId(nextBookId)
                        ?: throw ServiceException(ErrorType.CHAPTER_NOT_FOUND)
                }
            }

            DirectionType.PREV -> {
                if (chapterNumber > 1) {
                    targetChapterNumber -= 1
                } else if (currentBookIndex > 0) {
                    targetBook = books[currentBookIndex - 1]
                    val prevBookId = targetBook.id ?: throw IllegalStateException("Book has no ID")
                    targetMaxChapterNumber = bibleChapterRepository.findMaxChapterNumberByBookId(prevBookId)
                        ?: throw ServiceException(ErrorType.CHAPTER_NOT_FOUND)
                    targetChapterNumber = targetMaxChapterNumber
                }
            }
        }

        val targetBookId = targetBook.id ?: throw IllegalStateException("Book has no ID")
        val chapter = bibleChapterRepository.findByBookAndChapter(targetBookId, targetChapterNumber)
            ?: throw ServiceException(ErrorType.CHAPTER_NOT_FOUND)

        return BibleApiResponse.Verses.of(
            books = books,
            currentBook = targetBook,
            totalChapterCount = targetMaxChapterNumber,
            chapter = chapter
        )
    }

    fun searchBibleVerses(
        translationId: Long,
        keyword: String
    ): List<BibleSearchResponse> {
        if (keyword.isBlank()) throw ServiceException(ErrorType.INVALID_PARAMETER, "keyword is blank")
        return try {
            bibleVerseRepository.searchByTranslationAndText(translationId, keyword)
        } catch (e: Exception) {
            throw ServiceException(ErrorType.SEARCH_ERROR, "keyword=$keyword", e.message ?: "Unknown error")
        }
    }
}