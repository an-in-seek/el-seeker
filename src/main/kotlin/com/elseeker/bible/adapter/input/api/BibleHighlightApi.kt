package com.elseeker.bible.adapter.input.api

import com.elseeker.bible.adapter.input.api.request.BibleHighlightRequest
import com.elseeker.bible.adapter.input.api.response.BibleHighlightApiResponse
import com.elseeker.bible.application.service.BibleHighlightService
import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.member.application.service.MemberService
import com.elseeker.member.domain.model.Member
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}")
class BibleHighlightApi(
    private val bibleHighlightService: BibleHighlightService,
    private val memberService: MemberService
) {

    @GetMapping("/highlights")
    fun getChapterHighlights(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int
    ): ResponseEntity<List<BibleHighlightApiResponse.HighlightItem>> {
        val member = getMember(principal)
        val response = bibleHighlightService.getChapterHighlights(
            member,
            translationId,
            bookOrder,
            chapterNumber
        ).map(BibleHighlightApiResponse.HighlightItem::from)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/verses/{verseNumber}/highlight")
    fun upsertHighlight(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int,
        @PathVariable verseNumber: Int,
        @RequestBody request: BibleHighlightRequest
    ): ResponseEntity<BibleHighlightApiResponse.HighlightItem> {
        val member = getMember(principal)
        val highlight = bibleHighlightService.upsertHighlight(
            member = member,
            translationId = translationId,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber,
            verseNumber = verseNumber,
            colorValue = request.color
        )
        return ResponseEntity.ok(BibleHighlightApiResponse.HighlightItem.from(highlight))
    }

    @DeleteMapping("/verses/{verseNumber}/highlight")
    fun deleteHighlight(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int,
        @PathVariable verseNumber: Int
    ): ResponseEntity<Void> {
        val member = getMember(principal)
        bibleHighlightService.deleteHighlight(
            member,
            translationId,
            bookOrder,
            chapterNumber,
            verseNumber
        )
        return ResponseEntity.noContent().build()
    }

    private fun getMember(principal: JwtPrincipal): Member =
        memberService.getMember(principal.memberUid)
}
