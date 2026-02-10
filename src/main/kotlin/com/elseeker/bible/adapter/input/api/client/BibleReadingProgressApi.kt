package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.adapter.input.api.client.request.BibleReadingProgressRequest
import com.elseeker.bible.adapter.input.api.client.response.BibleReadingProgressResponse
import com.elseeker.bible.application.service.BibleReadingProgressService
import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.member.application.service.MemberService
import com.elseeker.member.domain.model.Member
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/bible/reading")
class BibleReadingProgressApi(
    private val bibleReadingProgressService: BibleReadingProgressService,
    private val memberService: MemberService
) {

    @PostMapping("/chapters/read")
    fun markChapterAsRead(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestBody request: BibleReadingProgressRequest
    ): ResponseEntity<Void> {
        val member = getMember(principal)
        bibleReadingProgressService.markChapterAsRead(
            member = member,
            translationId = request.translationId,
            bookOrder = request.bookOrder,
            chapterNumber = request.chapterNumber
        )
        return ResponseEntity.ok().build()
    }

    @GetMapping("/chapters/read")
    fun getReadChapters(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam translationId: Long,
        @RequestParam bookOrder: Int
    ): ResponseEntity<BibleReadingProgressResponse> {
        val chapterNumbers = bibleReadingProgressService.getReadChapters(
            memberUid = principal.memberUid,
            translationId = translationId,
            bookOrder = bookOrder
        )
        return ResponseEntity.ok(BibleReadingProgressResponse(chapterNumbers))
    }

    private fun getMember(principal: JwtPrincipal): Member =
        memberService.getMember(principal.memberUid)
}
