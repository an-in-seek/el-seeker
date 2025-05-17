package com.elseeker.bible.presentation.api

import com.elseeker.bible.domain.bible.model.BibleBook
import com.elseeker.bible.domain.bible.model.BibleChapter
import com.elseeker.bible.domain.bible.model.BibleTestamentType
import com.elseeker.bible.domain.bible.model.BibleTranslationType
import com.elseeker.bible.domain.bible.result.BibleResult

object BibleApiResponse {

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