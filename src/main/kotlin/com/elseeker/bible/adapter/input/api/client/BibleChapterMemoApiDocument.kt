package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.adapter.input.api.client.request.BibleChapterMemoRequest
import com.elseeker.bible.adapter.input.api.client.response.BibleChapterMemoApiResponse
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.http.ResponseEntity

interface BibleChapterMemoApiDocument {

    /**
     * 장 메모 조회
     */
    fun getChapterMemo(
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        principal: JwtPrincipal
    ): ResponseEntity<BibleChapterMemoApiResponse.ChapterMemoItem>

    /**
     * 장 메모 생성/수정 (upsert)
     */
    fun upsertChapterMemo(
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        principal: JwtPrincipal,
        request: BibleChapterMemoRequest
    ): ResponseEntity<BibleChapterMemoApiResponse.ChapterMemoItem>

    /**
     * 장 메모 삭제
     */
    fun deleteChapterMemo(
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        principal: JwtPrincipal
    ): ResponseEntity<Void>
}
