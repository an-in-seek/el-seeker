package com.elseeker.bible.presentation.api

import com.elseeker.bible.application.bible.BibleFacade
import com.elseeker.bible.domain.bible.DirectionType
import com.elseeker.bible.presentation.api.response.BibleSearchResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bibles")
class BibleApiController(
    private val bibleFacade: BibleFacade
) : BibleApiDocument {

    @GetMapping("/translations")
    override fun getTranslations(): ResponseEntity<List<BibleApiResponse.Translation>> {
        val response = bibleFacade.getTranslations().map(BibleApiResponse.Translation::from)
        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/translations/{translationId}/books")
    override fun getBooks(
        @PathVariable translationId: Long
    ): ResponseEntity<List<BibleApiResponse.Book>> {
        val response = bibleFacade.getBooks(translationId).map(BibleApiResponse.Book::from)
        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/translations/{translationId}/books/{bookOrder}")
    override fun getBook(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int
    ): ResponseEntity<BibleApiResponse.BookDetail> {
        val response = bibleFacade.getBook(translationId, bookOrder)
        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/translations/{translationId}/books/{bookOrder}/chapters")
    override fun getChapters(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int
    ): ResponseEntity<BibleApiResponse.Chapters> {
        val response = bibleFacade.getChapters(translationId, bookOrder)
        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/verses")
    override fun getChapterVerses(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int
    ): ResponseEntity<BibleApiResponse.Verses> {
        val response = bibleFacade.getChapterVerses(translationId, bookOrder, chapterNumber)
        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/navigate")
    override fun getAdjacentChapterVerses(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int,
        @RequestParam direction: DirectionType // "prev" or "next"
    ): ResponseEntity<BibleApiResponse.Verses> {
        val response = bibleFacade.getAdjacentChapterVerses(translationId, bookOrder, chapterNumber, direction)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/translations/{translationId}/search")
    override fun searchBible(
        @PathVariable translationId: Long,
        @RequestParam keyword: String
    ): ResponseEntity<List<BibleSearchResponse>> {
        val response = bibleFacade.searchBibleVerses(translationId, keyword)
        return ResponseEntity.ok(response)
    }
}
