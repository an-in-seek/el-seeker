package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.application.service.BibleService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bible")
class BibleTypingLookupApi(
    private val bibleService: BibleService
) {

    @GetMapping("/translations")
    fun getTranslations(): List<BibleTypingLookupResponse.Translation> {
        return bibleService.getTranslations().map {
            BibleTypingLookupResponse.Translation(
                id = it.translationId,
                name = it.translationName,
                code = it.translationType.name
            )
        }
    }

    @GetMapping("/books")
    fun getBooks(
        @RequestParam translationId: Long
    ): List<BibleTypingLookupResponse.Book> {
        return bibleService.getBooks(translationId).map {
            BibleTypingLookupResponse.Book(
                bookOrder = it.bookOrder,
                name = it.bookName
            )
        }
    }

    @GetMapping("/chapters")
    fun getChapters(
        @RequestParam translationId: Long,
        @RequestParam bookOrder: Int
    ): List<BibleTypingLookupResponse.Chapter> {
        val book = bibleService.getChapters(translationId, bookOrder).book
        return book.chapters.map {
            BibleTypingLookupResponse.Chapter(
                chapterNumber = it.chapterNumber
            )
        }
    }

    @GetMapping("/verses")
    fun getVerses(
        @RequestParam translationId: Long,
        @RequestParam bookOrder: Int,
        @RequestParam chapterNumber: Int
    ): List<BibleTypingLookupResponse.Verse> {
        val chapter = bibleService.getChapterVerses(translationId, bookOrder, chapterNumber).book.chapter
        return chapter.verses.map {
            BibleTypingLookupResponse.Verse(
                verseNumber = it.verseNumber,
                text = it.text
            )
        }
    }
}
