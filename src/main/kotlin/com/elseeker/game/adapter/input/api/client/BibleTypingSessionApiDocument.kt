package com.elseeker.game.adapter.input.api.client

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.client.request.BibleTypingSessionCreateRequest
import com.elseeker.game.adapter.input.api.client.request.BibleTypingSessionEndRequest
import com.elseeker.game.adapter.input.api.client.response.BibleTypingSessionResponse
import com.elseeker.game.adapter.input.api.client.response.BibleTypingSessionSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "Bible Typing Sessions", description = "Bible typing session management")
interface BibleTypingSessionApiDocument {

    @Operation(summary = "Create typing session")
    fun createSession(
        principal: JwtPrincipal,
        request: BibleTypingSessionCreateRequest
    ): ResponseEntity<BibleTypingSessionResponse>

    @Operation(summary = "End typing session")
    fun endSession(
        principal: JwtPrincipal,
        sessionKey: String,
        request: BibleTypingSessionEndRequest
    ): ResponseEntity<Void>

    @Operation(summary = "Reset typing session progress")
    fun resetSession(
        principal: JwtPrincipal,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): ResponseEntity<Void>

    @Operation(summary = "Get typing session summaries")
    fun getSessions(
        principal: JwtPrincipal,
        translationId: Long?,
        bookOrder: Int?,
        chapterNumber: Int?
    ): ResponseEntity<BibleTypingSessionSummaryResponse>
}
