package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.adapter.input.api.client.response.BibleChapterStateResponse
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.http.ResponseEntity

interface BibleChapterViewApiDocument {

    /**
     * 📌 성경 장(Chapter) 사용자 상태 조회 (메모/형광펜/읽음)
     */
    fun getChapterState(
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        principal: JwtPrincipal
    ): ResponseEntity<BibleChapterStateResponse>
}
