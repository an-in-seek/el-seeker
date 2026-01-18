package com.elseeker.game.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.dto.BibleTypingSessionDto
import com.elseeker.game.application.service.BibleTypingSessionService
import com.elseeker.member.application.service.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/game/bible-typing/sessions")
class BibleTypingSessionApi(
    private val bibleTypingSessionService: BibleTypingSessionService,
    private val memberService: MemberService
) {

    @PostMapping
    fun createSession(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestBody request: BibleTypingSessionDto.CreateRequest
    ): ResponseEntity<BibleTypingSessionDto.Response> {
        val member = memberService.getMember(principal.memberUid)
        val session = bibleTypingSessionService.createSession(member, request)
        return ResponseEntity.ok(
            BibleTypingSessionDto.Response(
                sessionId = session.id!!,
                createdAt = session.createdAt
            )
        )
    }

    @GetMapping
    fun getSessions(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam(required = false) translationId: Long?,
        @RequestParam(required = false) bookOrder: Int?,
        @RequestParam(required = false) chapterNumber: Int?,
        @RequestParam(required = false) fromDate: LocalDate?,
        @RequestParam(required = false) toDate: LocalDate?
    ): ResponseEntity<List<BibleTypingSessionDto.SummaryResponse>> {
        val member = memberService.getMember(principal.memberUid)
        val sessions = bibleTypingSessionService.getSessions(
            member = member,
            translationId = translationId,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber,
            fromDate = fromDate,
            toDate = toDate
        )
        val response = sessions.map {
            BibleTypingSessionDto.SummaryResponse(
                sessionId = it.id!!,
                translationId = it.translationId,
                bookOrder = it.bookOrder,
                chapterNumber = it.chapterNumber,
                totalVerses = it.totalVerses,
                completedVerses = it.completedVerses,
                accuracy = it.accuracy,
                cpm = it.cpm,
                createdAt = it.createdAt
            )
        }
        return ResponseEntity.ok(response)
    }
}
