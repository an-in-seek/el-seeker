package com.elseeker.game.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionCreateRequest
import com.elseeker.game.adapter.input.api.request.BibleTypingSessionUpdateRequest
import com.elseeker.game.adapter.input.api.response.BibleTypingSessionResponse
import com.elseeker.game.adapter.input.api.response.BibleTypingSessionSummaryResponse
import com.elseeker.game.application.service.BibleTypingSessionService
import com.elseeker.member.application.service.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/game/bible-typing/sessions")
class BibleTypingSessionApi(
    private val bibleTypingSessionService: BibleTypingSessionService,
    private val memberService: MemberService
) {

    @PostMapping
    fun createSession(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestBody request: BibleTypingSessionCreateRequest
    ): ResponseEntity<BibleTypingSessionResponse> {
        val member = memberService.getMember(principal.memberUid)
        val session = bibleTypingSessionService.createSession(member, request)
        return ResponseEntity.ok(
            BibleTypingSessionResponse(
                sessionKey = session.sessionKey.toString(),
                createdAt = session.createdAt
            )
        )
    }

    @PutMapping("/{sessionKey}")
    fun updateSession(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable sessionKey: String,
        @RequestBody request: BibleTypingSessionUpdateRequest
    ): ResponseEntity<Void> {
        val member = memberService.getMember(principal.memberUid)
        bibleTypingSessionService.updateSession(member, sessionKey, request)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getSessions(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam(required = false) translationId: Long?,
        @RequestParam(required = false) bookOrder: Int?,
        @RequestParam(required = false) chapterNumber: Int?,
    ): ResponseEntity<BibleTypingSessionSummaryResponse> {
        val member = memberService.getMember(principal.memberUid)
        val session = bibleTypingSessionService.getSessions(
            member,
            translationId,
            bookOrder,
            chapterNumber,
        ).let(BibleTypingSessionSummaryResponse::of)
        return ResponseEntity.ok(session)
    }
}
