package com.elseeker.bible.domain.result

import com.elseeker.bible.domain.model.*
import com.elseeker.bible.domain.vo.BibleTestamentType
import com.elseeker.bible.domain.vo.BibleTranslationType
import com.neovisionaries.i18n.LanguageCode

object BibleResult {

    data class Translation(
        val translationId: Long,
        val translationType: BibleTranslationType,
        val translationName: String,
        val translationLanguage: LanguageCode
    ) {
        companion object {
            fun from(translation: BibleTranslation) = with(translation) {
                Translation(
                    translationId = id!!,
                    translationType = translationType,
                    translationName = name,
                    translationLanguage = languageCode
                )
            }
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
            fun from(book: BibleBook) = with(book) {
                Book(
                    bookId = id!!,
                    bookOrder = bookOrder,
                    bookName = name,
                    abbreviation = abbreviation,
                    testamentType = testamentType,
                    chapterCount = chapters.size
                )
            }
        }
    }

    data class BookDetail(
        val bookId: Long,
        val bookName: String,
        val abbreviation: String,
        val descriptionSummary: String,
        val chapters: List<Chapter>
    ) {
        companion object {
            fun from(book: BibleBook, description: BibleBookDescription) = with(book) {
                BookDetail(
                    bookId = id!!,
                    bookName = name,
                    abbreviation = abbreviation,
                    descriptionSummary = description.summary,
                    chapters = book.chapters.map(Chapter::from)
                )
            }
        }
    }

    data class Chapter(
        val chapterId: Long,
        val chapterNumber: Int
    ) {
        companion object {
            fun from(chapter: BibleChapter) = with(chapter) {
                Chapter(
                    chapterId = id!!,
                    chapterNumber = chapterNumber
                )
            }
        }
    }

    data class Verse(
        val verseId: Long,
        val verseNumber: Int,
        val text: String
    ) {
        companion object {
            fun from(verse: BibleVerse) = with(verse) {
                Verse(
                    verseId = id!!,
                    verseNumber = verseNumber,
                    text = text
                )
            }
        }
    }

    data class ChapterDetail(
        val chapterId: Long,
        val chapterNumber: Int,
        val verses: List<Verse>,
    ) {
        companion object {
            fun from(chapter: BibleChapter): ChapterDetail {
                return ChapterDetail(
                    chapterId = chapter.id!!,
                    chapterNumber = chapter.chapterNumber,
                    verses = chapter.verses.map(Verse::from),
                )
            }
        }
    }

}
