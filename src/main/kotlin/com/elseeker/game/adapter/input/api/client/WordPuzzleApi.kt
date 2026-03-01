package com.elseeker.game.adapter.input.api.client

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.client.request.*
import com.elseeker.game.adapter.input.api.client.response.*
import com.elseeker.game.application.service.WordPuzzleService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/v1/game/word-puzzles")
class WordPuzzleApi(
    private val wordPuzzleService: WordPuzzleService
) : WordPuzzleApiDocument {

    @GetMapping
    override fun getPuzzles(
        @RequestParam(required = false) theme: String?,
        @RequestParam(required = false) difficulty: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<Page<PuzzleSummaryResponse>> {
        val pageable = PageRequest.of(page, size)
        val response = wordPuzzleService.getPuzzles(theme, difficulty, pageable, principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{puzzleId}/attempts")
    override fun startPuzzle(
        @PathVariable puzzleId: Long,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<PuzzleAttemptResponse> {
        val response = wordPuzzleService.startPuzzle(puzzleId, principal.memberUid)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{puzzleId}/attempts/{attemptId}")
    override fun resumeAttempt(
        @PathVariable puzzleId: Long,
        @PathVariable attemptId: Long,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<PuzzleAttemptResponse> {
        val response = wordPuzzleService.resumeAttempt(puzzleId, attemptId, principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{puzzleId}/attempts/{attemptId}/cells")
    override fun saveCells(
        @PathVariable puzzleId: Long,
        @PathVariable attemptId: Long,
        @Valid @RequestBody request: CellSaveRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<Void> {
        wordPuzzleService.saveCells(puzzleId, attemptId, request, principal.memberUid)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{puzzleId}/attempts/{attemptId}/hints/reveal-letter")
    override fun revealLetter(
        @PathVariable puzzleId: Long,
        @PathVariable attemptId: Long,
        @Valid @RequestBody request: RevealLetterRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<RevealLetterResponse> {
        val response = wordPuzzleService.revealLetter(puzzleId, attemptId, request, principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{puzzleId}/attempts/{attemptId}/hints/check-word")
    override fun checkWord(
        @PathVariable puzzleId: Long,
        @PathVariable attemptId: Long,
        @Valid @RequestBody request: CheckWordRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<CheckWordResponse> {
        val response = wordPuzzleService.checkWord(puzzleId, attemptId, request, principal.memberUid)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{puzzleId}/attempts/{attemptId}/submit")
    override fun submit(
        @PathVariable puzzleId: Long,
        @PathVariable attemptId: Long,
        @Valid @RequestBody request: SubmitRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<Any> {
        val response = wordPuzzleService.submit(puzzleId, attemptId, request, principal.memberUid)
        return ResponseEntity.ok(response)
    }
}
