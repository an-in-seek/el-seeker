package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.adapter.input.api.client.request.BibleBookMemoRequest
import com.elseeker.bible.adapter.input.api.client.response.BibleBookMemoApiResponse
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.http.ResponseEntity

interface BibleBookMemoApiDocument {

    /**
     * 책 메모 조회
     */
    fun getBookMemo(
        translationId: Long,
        bookOrder: Int,
        principal: JwtPrincipal
    ): ResponseEntity<BibleBookMemoApiResponse.BookMemoItem>

    /**
     * 책 메모 생성/수정 (upsert)
     */
    fun upsertBookMemo(
        translationId: Long,
        bookOrder: Int,
        principal: JwtPrincipal,
        request: BibleBookMemoRequest
    ): ResponseEntity<BibleBookMemoApiResponse.BookMemoItem>

    /**
     * 책 메모 삭제
     */
    fun deleteBookMemo(
        translationId: Long,
        bookOrder: Int,
        principal: JwtPrincipal
    ): ResponseEntity<Void>
}
