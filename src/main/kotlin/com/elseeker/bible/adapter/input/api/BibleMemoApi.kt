package com.elseeker.bible.adapter.input.api

import com.elseeker.bible.adapter.input.api.request.BibleMemoRequest
import com.elseeker.bible.adapter.input.api.response.BibleMemoApiResponse
import com.elseeker.bible.application.service.BibleMemoService
import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.member.application.service.MemberService
import com.elseeker.member.domain.model.Member
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}")
class BibleMemoApi(
    private val bibleMemoService: BibleMemoService,
    private val memberService: MemberService
) {

    @GetMapping("/memos")
    fun getChapterMemos(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int
    ): ResponseEntity<List<BibleMemoApiResponse.MemoItem>> {
        val member = getMember(principal)
        val response = bibleMemoService.getChapterMemos(
            member,
            translationId,
            bookOrder,
            chapterNumber
        ).map(BibleMemoApiResponse.MemoItem::from)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/verses/{verseNumber}/memo")
    fun upsertMemo(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int,
        @PathVariable verseNumber: Int,
        @RequestBody request: BibleMemoRequest
    ): ResponseEntity<BibleMemoApiResponse.MemoItem> {
        val member = getMember(principal)
        val memo = bibleMemoService.upsertMemo(
            member = member,
            translationId = translationId,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber,
            verseNumber = verseNumber,
            content = request.content
        )
        return ResponseEntity.ok(BibleMemoApiResponse.MemoItem.from(memo))
    }

    @DeleteMapping("/verses/{verseNumber}/memo")
    fun deleteMemo(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int,
        @PathVariable verseNumber: Int
    ): ResponseEntity<Void> {
        val member = getMember(principal)
        bibleMemoService.deleteMemo(
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
