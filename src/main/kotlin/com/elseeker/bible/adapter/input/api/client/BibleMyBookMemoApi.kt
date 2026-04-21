package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.application.service.BibleBookMemoService
import com.elseeker.bible.domain.result.BibleBookMemoResult
import com.elseeker.bible.domain.result.BibleMemoResult
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bibles/my-book-memos")
class BibleMyBookMemoApi(
    private val bibleBookMemoService: BibleBookMemoService
) {

    @GetMapping("/translations")
    fun getMyMemoTranslations(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<List<BibleMemoResult.MemoTranslationItem>> {
        val result = bibleBookMemoService.getMemoTranslations(principal.memberUid)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/books")
    fun getMyMemoBooks(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam translationId: Long
    ): ResponseEntity<List<BibleMemoResult.MemoBookItem>> {
        val result = bibleBookMemoService.getMemoBookList(principal.memberUid, translationId)
        return ResponseEntity.ok(result)
    }

    @GetMapping
    fun getMyBookMemos(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) translationId: Long?,
        @RequestParam(required = false) bookOrder: Int?
    ): ResponseEntity<BibleBookMemoResult.BookMemoSlice> {
        val effectiveSize = size.coerceIn(1, 100)
        val pageable = PageRequest.of(page, effectiveSize)
        val result = bibleBookMemoService.getMyBookMemos(principal.memberUid, pageable, translationId, bookOrder)
        return ResponseEntity.ok(result)
    }
}
