package com.seek.thebible.presentation.bible

import com.seek.thebible.application.bible.BibleFacade
import com.seek.thebible.domain.DirectionType
import com.seek.thebible.presentation.bible.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bibles")
class BibleController(
    private val bibleFacade: BibleFacade
) {

    /**
     * 📌 번역본(Translation) 리스트 조회
     */
    @GetMapping("/translations")
    fun getTranslations(): ResponseEntity<List<TranslationResponse>> {
        val response = bibleFacade.getTranslations().map(TranslationResponse::from)
        return ResponseEntity.ok().body(response)
    }

    /**
     * 📌 특정 번역본(Translation)에 해당하는 책(Book) 리스트 조회
     */
    @GetMapping("/translations/{translationId}/books")
    fun getBooks(
        @PathVariable translationId: Long
    ): ResponseEntity<List<BookResponse>> {
        val response = bibleFacade.getBooks(translationId).map(BookResponse::from)
        return ResponseEntity.ok().body(response)
    }

    /**
     * 📌 특정 책(Book)에 해당하는 장(Chapter) 리스트 조회
     */
    @GetMapping("/translations/{translationId}/books/{bookId}/chapters")
    fun getChapters(
        @PathVariable translationId: Long,
        @PathVariable bookId: Long
    ): ResponseEntity<ChapterViewResponse> {
        val response = bibleFacade.getChapterView(bookId).let(ChapterViewResponse::from)
        return ResponseEntity.ok().body(response)
    }

    /**
     * 📌 특정 장(Chapter)에 해당하는 절(Verse) 리스트 조회
     */
    @GetMapping("/translations/{translationId}/books/{bookId}/chapters/{chapterNumber}/verses")
    fun getVerses(
        @PathVariable translationId: Long,
        @PathVariable bookId: Long,
        @PathVariable chapterNumber: Int
    ): ResponseEntity<VerseViewResponse> {
        val result = bibleFacade.getVerseView(translationId, bookId, chapterNumber)
        val response = VerseViewResponse.from(result)
        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/translations/{translationId}/books/{bookId}/chapters/{chapterNumber}/navigate")
    fun navigateChapter(
        @PathVariable translationId: Long,
        @PathVariable bookId: Long,
        @PathVariable chapterNumber: Int,
        @RequestParam direction: DirectionType // "prev" or "next"
    ): ResponseEntity<VerseViewResponse> {
        val result = bibleFacade.navigate(translationId, bookId, chapterNumber, direction)
        val response = VerseViewResponse.from(result)
        return ResponseEntity.ok(response)
    }

    /**
     * 📌 성경 구절 검색 (키워드 포함)
     */
    @GetMapping("/search")
    fun searchBible(@RequestParam keyword: String): ResponseEntity<List<SearchVerseResponse>> {
        val response = bibleFacade.searchBibleVerses(keyword).map(SearchVerseResponse::from)
        return ResponseEntity.ok(response)
    }
}
