package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.adapter.input.api.client.request.BibleChapterMemoRequest
import com.elseeker.bible.adapter.input.api.client.response.BibleChapterMemoApiResponse
import com.elseeker.bible.application.service.BibleChapterMemoService
import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.member.application.service.MemberService
import com.elseeker.member.domain.model.Member
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}")
class BibleChapterMemoApi(
    private val bibleChapterMemoService: BibleChapterMemoService,
    private val memberService: MemberService
) : BibleChapterMemoApiDocument {

    @GetMapping("/chapter-memo")
    override fun getChapterMemo(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<BibleChapterMemoApiResponse.ChapterMemoItem> {
        val memo = bibleChapterMemoService.getChapterMemo(
            principal.memberUid,
            translationId,
            bookOrder,
            chapterNumber
        ) ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(BibleChapterMemoApiResponse.ChapterMemoItem.from(memo))
    }

    @PutMapping("/chapter-memo")
    override fun upsertChapterMemo(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestBody request: BibleChapterMemoRequest
    ): ResponseEntity<BibleChapterMemoApiResponse.ChapterMemoItem> {
        val member = getMember(principal)
        val memo = bibleChapterMemoService.upsertChapterMemo(
            member = member,
            translationId = translationId,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber,
            content = request.content
        )
        return ResponseEntity.ok(BibleChapterMemoApiResponse.ChapterMemoItem.from(memo))
    }

    @DeleteMapping("/chapter-memo")
    override fun deleteChapterMemo(
        @PathVariable translationId: Long,
        @PathVariable bookOrder: Int,
        @PathVariable chapterNumber: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<Void> {
        val member = getMember(principal)
        bibleChapterMemoService.deleteChapterMemo(
            member,
            translationId,
            bookOrder,
            chapterNumber
        )
        return ResponseEntity.noContent().build()
    }

    private fun getMember(principal: JwtPrincipal): Member =
        memberService.getMember(principal.memberUid)
}
