package com.elseeker.game.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.request.BibleTypingVerseProgressRequest
import com.elseeker.game.adapter.input.api.response.BibleTypingVersesResponse
import com.elseeker.game.application.service.BibleTypingSessionService
import com.elseeker.member.application.service.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/game/bible-typing/progress")
class BibleTypingVerseProgressApi(
    private val bibleTypingSessionService: BibleTypingSessionService,
    private val memberService: MemberService
) {

    // 절(Verse) 진행 정보 저장
    @PostMapping("/verses")
    fun saveProgress(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestBody request: BibleTypingVerseProgressRequest
    ): ResponseEntity<Void> {
        val member = memberService.getMember(principal.memberUid)
        bibleTypingSessionService.saveVerseProgress(member, request)
        return ResponseEntity.noContent().build()
    }

    // 특정 범위 진행 정보 조회
    @GetMapping
    fun getProgress(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam translationId: Long,
        @RequestParam bookOrder: Int,
        @RequestParam chapterNumber: Int
    ): ResponseEntity<BibleTypingVersesResponse> {
        val member = memberService.getMember(principal.memberUid)
        val response = bibleTypingSessionService.getProgress(member, translationId, bookOrder, chapterNumber)
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(response)
    }

    // 최신 진행 정보 조회
    @GetMapping("/latest")
    fun getLatestProgress(@AuthenticationPrincipal principal: JwtPrincipal): ResponseEntity<BibleTypingVersesResponse> {
        val member = memberService.getMember(principal.memberUid)
        val response = bibleTypingSessionService.getProgress(member)
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(response)
    }
}
