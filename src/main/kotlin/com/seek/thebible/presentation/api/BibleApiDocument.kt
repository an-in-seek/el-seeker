package com.seek.thebible.presentation.api

import com.seek.thebible.domain.DirectionType
import com.seek.thebible.presentation.api.response.BibleSearchResponse
import com.seek.thebible.presentation.web.response.BibleViewResponse
import org.springframework.http.ResponseEntity

interface BibleApiDocument {

    /**
     * 📌 번역본(Translation) 리스트 조회
     */
    fun getTranslations(): ResponseEntity<List<BibleApiResponse.Translation>>

    /**
     * 📌 특정 번역본(Translation)에 해당하는 책(Book) 리스트 조회
     */
    fun getBooks(
        translationId: Long
    ): ResponseEntity<List<BibleApiResponse.Book>>

    /**
     * 📌 특정 책(Book)에 해당하는 장(Chapter) 리스트 조회
     */
    fun getChapters(
        translationId: Long,
        bookId: Long
    ): ResponseEntity<BibleViewResponse.Chapter>

    /**
     * 📌 특정 장(Chapter)에 해당하는 절(Verse) 리스트 조회
     */
    fun getChapterVerses(
        translationId: Long,
        bookId: Long,
        chapterNumber: Int
    ): ResponseEntity<BibleApiResponse.Verse>

    fun getAdjacentChapterVerses(
        translationId: Long,
        bookId: Long,
        chapterNumber: Int,
        direction: DirectionType // "prev" or "next"
    ): ResponseEntity<BibleApiResponse.Verse>

    /**
     * 📌 성경 구절 검색 (키워드 포함)
     */
    fun searchBible(
        translationId: Long,
        keyword: String
    ): ResponseEntity<List<BibleSearchResponse>>
}
