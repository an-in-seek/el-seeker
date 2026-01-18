package com.elseeker.game.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.dto.BibleTypingVerseProgressRequest
import com.elseeker.game.application.service.BibleTypingVerseProgressService
import com.elseeker.member.application.service.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/game/bible-typing/verse-results")
class BibleTypingVerseProgressApi(
    private val bibleTypingVerseProgressService: BibleTypingVerseProgressService,
    private val memberService: MemberService
) {

    @PostMapping
    fun saveProgress(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestBody request: BibleTypingVerseProgressRequest
    ): ResponseEntity<Void> {
        val member = memberService.getMember(principal.memberUid)
        bibleTypingVerseProgressService.saveProgress(member, request)
        return ResponseEntity.noContent().build()
    }
}
