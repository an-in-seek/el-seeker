package com.elseeker.game.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.request.BibleTypingVerseProgressRequest
import com.elseeker.game.adapter.input.api.response.BibleTypingVerseProgressResponse
import com.elseeker.game.application.service.BibleTypingSessionService
import com.elseeker.member.application.service.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/game/bible-typing/verse-results")
class BibleTypingVerseProgressApi(
    private val bibleTypingSessionService: BibleTypingSessionService,
    private val memberService: MemberService
) {

    @PostMapping
    fun saveProgress(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestBody request: BibleTypingVerseProgressRequest
    ): ResponseEntity<Void> {
        val member = memberService.getMember(principal.memberUid)
        bibleTypingSessionService.saveVerseProgress(member, request)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getLatestProgress(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam translationId: Long,
        @RequestParam bookOrder: Int,
        @RequestParam chapterNumber: Int
    ): ResponseEntity<BibleTypingVerseProgressResponse> {
        val member = memberService.getMember(principal.memberUid)
        val response = bibleTypingSessionService.getLatestProgress(
            member,
            translationId,
            bookOrder,
            chapterNumber
        ) ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/latest")
    fun getLatestProgress(@AuthenticationPrincipal principal: JwtPrincipal): ResponseEntity<BibleTypingVerseProgressResponse> {
        val member = memberService.getMember(principal.memberUid)
        val response = bibleTypingSessionService.getLatestProgress(member) ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(response)
    }
}
