package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.adapter.input.api.client.response.BibleChapterStateResponse
import com.elseeker.bible.adapter.input.api.client.response.BibleHighlightApiResponse
import com.elseeker.bible.adapter.input.api.client.response.BibleMemoApiResponse
import com.elseeker.bible.application.service.BibleHighlightService
import com.elseeker.bible.application.service.BibleMemoService
import com.elseeker.bible.application.service.BibleReadingProgressService
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}")
class BibleChapterViewApi(
    private val bibleMemoService: BibleMemoService,
    private val bibleHighlightService: BibleHighlightService,
    private val bibleReadingProgressService: BibleReadingProgressService
) : BibleChapterViewApiDocument {

    private val privateNoStore = CacheControl.noStore().cachePrivate()

    @GetMapping("/state")
    override fun getChapterState(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<BibleChapterStateResponse> {
        val memos = bibleMemoService.getChapterMemos(
            principal.memberUid,
            translationId,
            bookOrder,
            chapterNumber
        ).map(BibleMemoApiResponse.MemoItem::from)
        val highlights = bibleHighlightService.getChapterHighlights(
            principal.memberUid,
            translationId,
            bookOrder,
            chapterNumber
        ).map(BibleHighlightApiResponse.HighlightItem::from)
        val isRead = bibleReadingProgressService.isChapterRead(
            principal.memberUid,
            translationId,
            bookOrder,
            chapterNumber
        )
        return ResponseEntity.ok()
            .cacheControl(privateNoStore)
            .body(
                BibleChapterStateResponse(
                    memos = memos,
                    highlights = highlights,
                    isRead = isRead
                )
            )
    }
}
