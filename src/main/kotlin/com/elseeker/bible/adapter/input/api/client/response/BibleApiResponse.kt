package com.elseeker.bible.adapter.input.api.client.response

import com.elseeker.bible.domain.model.BibleBook
import com.elseeker.bible.domain.model.BibleBookDescription
import com.elseeker.bible.domain.model.BibleChapter
import com.elseeker.bible.domain.result.BibleResult
import com.elseeker.bible.domain.vo.BibleTestamentType
import com.elseeker.bible.domain.vo.BibleTranslationType
import com.neovisionaries.i18n.LanguageCode

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
            fun from(bibleBook: BibleBook, description: BibleBookDescription): BookDetail {
                return BookDetail(
                    bookId = bibleBook.id!!,
                    bookOrder = bibleBook.bookOrder,
                    bookName = bibleBook.name,
                    abbreviation = bibleBook.abbreviation,
                    testamentType = bibleBook.testamentType,
                    description = Description(
                        summary = description.summary,
                        author = description.author,
                        writtenYear = description.writtenYear,
                        historicalPeriod = description.historicalPeriod,
                        background = description.background,
                        content = description.content,
                    ),
                )
            }
        }
    }

    data class Chapters(
        val book: BibleResult.BookDetail
    ) {
        companion object {
            fun from(book: BibleBook, description: BibleBookDescription) =
                Chapters(
                    book = BibleResult.BookDetail.from(book, description)
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