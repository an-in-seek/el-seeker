package com.seek.thebible.presentation.api

import com.seek.thebible.application.bible.BibleFacade
import com.seek.thebible.domain.DirectionType
import com.seek.thebible.presentation.api.response.BibleSearchResponse
import com.seek.thebible.presentation.web.response.BibleViewResponse
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

    @GetMapping("/translations/{translationId}/books/{bookId}/chapters")
    override fun getChapters(
        @PathVariable translationId: Long,
        @PathVariable bookId: Long
    ): ResponseEntity<BibleViewResponse.Chapter> {
        val response = bibleFacade.getChapterView(bookId)
        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/translations/{translationId}/books/{bookId}/chapters/{chapterNumber}/verses")
    override fun getChapterVerses(
        @PathVariable translationId: Long,
        @PathVariable bookId: Long,
        @PathVariable chapterNumber: Int
    ): ResponseEntity<BibleApiResponse.Verse> {
        val response = bibleFacade.getChapterVerses(translationId, bookId, chapterNumber)
        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/translations/{translationId}/books/{bookId}/chapters/{chapterNumber}/navigate")
    override fun getAdjacentChapterVerses(
        @PathVariable translationId: Long,
        @PathVariable bookId: Long,
        @PathVariable chapterNumber: Int,
        @RequestParam direction: DirectionType // "prev" or "next"
    ): ResponseEntity<BibleApiResponse.Verse> {
        val response = bibleFacade.getAdjacentChapterVerses(translationId, bookId, chapterNumber, direction)
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
