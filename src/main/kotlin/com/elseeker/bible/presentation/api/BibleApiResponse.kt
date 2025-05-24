package com.elseeker.bible.presentation.api

import com.elseeker.bible.domain.bible.model.*
import com.elseeker.bible.domain.bible.result.BibleResult

object BibleApiResponse {

    data class Translation(
        val translationId: Long,
        val translationType: BibleTranslationType,
        val translationName: String,
        val translationLanguage: LanguageCode
    ) {
        companion object {
            fun from(result: BibleResult.Translation) =
                Translation(
                    translationId = result.translationId,
                    translationType = result.translationType,
                    translationName = result.translationName,
                    translationLanguage = result.translationLanguage
                )
        }
    }

    data class Book(
        val bookId: Long,
        val bookOrder: Int,
        val bookName: String,
        val abbreviation: String,
        val testamentType: BibleTestamentType,
        val chapterCount: Int,
    ) {
        companion object {
            fun from(result: BibleResult.Book) =
                Book(
                    bookId = result.bookId,
                    bookOrder = result.bookOrder,
                    bookName = result.bookName,
                    abbreviation = result.abbreviation,
                    testamentType = result.testamentType,
                    chapterCount = result.chapterCount
                )
        }
    }

    data class BookDetail(
        val bookId: Long,
        val bookOrder: Int,
        val bookName: String,
        val abbreviation: String,
        val testamentType: BibleTestamentType,
        val description: Description,
    ) {
        data class Description(
            val summary: String,
            val author: String,
            val writtenYear: String,
            val historicalPeriod: String,
            val background: String,
            val content: String,
        )

        companion object {
            fun from(bibleBook: BibleBook): BookDetail {
                return BookDetail(
                    bookId = bibleBook.id!!,
                    bookOrder = bibleBook.bookOrder,
                    bookName = bibleBook.name,
                    abbreviation = bibleBook.abbreviation,
                    testamentType = bibleBook.testamentType,
                    description = Description(
                        summary = bibleBook.description.summary,
                        author = bibleBook.description.author,
                        writtenYear = bibleBook.description.writtenYear,
                        historicalPeriod = bibleBook.description.historicalPeriod,
                        background = bibleBook.description.background,
                        content = bibleBook.description.content,
                    ),
                )
            }
        }
    }

    data class Chapters(
        val book: BibleResult.BookDetail
    ) {
        companion object {
            fun from(book: BibleBook) =
                Chapters(
                    book = book.let(BibleResult.BookDetail::from)
                )
        }
    }

    data class Verses(
        val book: Book,
        val hasPrev: Boolean,
        val hasNext: Boolean,
        val isFirst: Boolean,
        val isLast: Boolean?,
    ) {
        data class Book(
            val bookId: Long,
            val bookOrder: Int,
            val bookName: String,
            val totalChapterCount: Int,
            val chapter: BibleResult.ChapterDetail,
        )

        companion object {
            fun of(
                books: List<BibleBook>,
                currentBook: BibleBook,
                totalChapterCount: Int,
                chapter: BibleChapter,
            ): Verses {
                val isFirst = currentBook.bookOrder == 1 && chapter.chapterNumber == 1
                val isLast = currentBook.bookOrder == books.last().bookOrder && chapter.chapterNumber == totalChapterCount
                val hasPrev = !isFirst
                val hasNext = !isLast
                return Verses(
                    book = Book(
                        bookId = currentBook.id!!,
                        bookOrder = currentBook.bookOrder,
                        bookName = currentBook.name,
                        totalChapterCount = totalChapterCount,
                        chapter = BibleResult.ChapterDetail.from(chapter),
                    ),
                    hasPrev = hasPrev,
                    hasNext = hasNext,
                    isFirst = isFirst,
                    isLast = isLast
                )
            }
        }
    }

}