package com.seek.thebible.domain.bible.dto

import com.seek.thebible.domain.bible.model.*

data class TranslationResult(
    val translationId: Long,
    val translationType: BibleTranslationType,
    val translationName: String
) {
    companion object {
        fun from(translation: BibleTranslation) = with(translation) {
            TranslationResult(
                translationId = id!!,
                translationType = translationType,
                translationName = name
            )
        }
    }
}

data class BookResult(
    val bookId: Long,
    val bookName: String,
    val abbreviation: String,
    val testamentType: BibleTestamentType,
    val chapterCount: Int,
) {
    companion object {
        fun from(book: BibleBook) = with(book) {
            BookResult(
                bookId = id!!,
                bookName = name,
                abbreviation = abbreviation,
                testamentType = testamentType,
                chapterCount = chapters.size
            )
        }
    }
}

data class ChapterView(
    val book: BookDetailResult,
) {
    companion object {
        fun from(book: BibleBook) =
            ChapterView(
                book = book.let(BookDetailResult::from)
            )
    }
}

data class BookDetailResult(
    val bookId: Long,
    val bookName: String,
    val abbreviation: String,
    val chapters: List<ChapterResult>
) {
    companion object {
        fun from(book: BibleBook) = with(book) {
            BookDetailResult(
                bookId = id!!,
                bookName = name,
                abbreviation = abbreviation,
                chapters = book.chapters.map(ChapterResult::from)
            )
        }
    }
}

data class ChapterResult(
    val chapterId: Long,
    val chapterNumber: Int
) {
    companion object {
        fun from(chapter: BibleChapter) = with(chapter) {
            ChapterResult(
                chapterId = id!!,
                chapterNumber = chapterNumber
            )
        }
    }
}


data class VerseViewResult(
    val book: Book,
    val hasPrev: Boolean,
    val hasNext: Boolean,
    val isFirst: Boolean,
    val isLast: Boolean?,
) {
    data class Book(
        val bookId: Long,
        val bookName: String,
        val totalChapterCount: Int,
        val chapter: ChapterDetailResult,
    )

    companion object {
        fun of(
            books: List<BibleBook>,
            currentBook: BibleBook,
            totalChapterCount: Int,
            chapter: BibleChapter,
        ): VerseViewResult {
            val isFirst = currentBook.bookOrder == 1 && chapter.chapterNumber == 1
            val isLast = currentBook.bookOrder == books.last().bookOrder && chapter.chapterNumber == totalChapterCount
            val hasPrev = !isFirst
            val hasNext = !isLast
            return VerseViewResult(
                book = Book(
                    bookId = currentBook.id!!,
                    bookName = currentBook.name,
                    totalChapterCount = totalChapterCount,
                    chapter = ChapterDetailResult.from(chapter),
                ),
                hasPrev = hasPrev,
                hasNext = hasNext,
                isFirst = isFirst,
                isLast = isLast
            )
        }
    }
}

data class ChapterDetailResult(
    val chapterId: Long,
    val chapterNumber: Int,
    val verses: List<VerseResult>,
) {
    companion object {
        fun from(chapter: BibleChapter): ChapterDetailResult {
            return ChapterDetailResult(
                chapterId = chapter.id!!,
                chapterNumber = chapter.chapterNumber,
                verses = chapter.verses.map(VerseResult::from),
            )
        }
    }
}

data class VerseResult(
    val verseId: Long,
    val verseNumber: Int,
    val text: String
) {
    companion object {
        fun from(verse: BibleVerse) = with(verse) {
            VerseResult(
                verseId = id!!,
                verseNumber = verseNumber,
                text = text
            )
        }
    }
}

data class SearchVerseResult(
    val verseId: Long,
    val verseNumber: Int,
    val text: String
)
