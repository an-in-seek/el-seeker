package com.elseeker.game.adapter.input.api.admin

import com.elseeker.common.adapter.input.api.admin.response.AdminPageResponse
import com.elseeker.game.adapter.input.api.admin.request.AdminWordPuzzleEntryRequest
import com.elseeker.game.adapter.input.api.admin.request.AdminWordPuzzleRequest
import com.elseeker.game.adapter.input.api.admin.request.AdminWordPuzzleStatusRequest
import com.elseeker.game.application.service.AdminWordPuzzleService
import com.elseeker.game.domain.model.WordPuzzle
import com.elseeker.game.domain.model.WordPuzzleEntry
import com.elseeker.study.domain.model.Dictionary
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/v1/admin/word-puzzles")
class AdminWordPuzzleApi(
    private val adminWordPuzzleService: AdminWordPuzzleService,
) {

    // ── A-1. 퍼즐 목록 조회 ──

    @GetMapping
    fun listPuzzles(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<AdminPageResponse<PuzzleItem>> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = adminWordPuzzleService.findAllPuzzles(pageable)
        return ResponseEntity.ok(AdminPageResponse.from(result) { PuzzleItem.from(it) })
    }

    // ── A-2. 퍼즐 단건 조회 ──

    @GetMapping("/{id}")
    fun getPuzzle(@PathVariable id: Long): ResponseEntity<PuzzleItem> =
        ResponseEntity.ok(PuzzleItem.from(adminWordPuzzleService.findPuzzleById(id)))

    // ── A-3. 퍼즐 등록 ──

    @PostMapping
    fun createPuzzle(@RequestBody request: AdminWordPuzzleRequest): ResponseEntity<PuzzleItem> {
        val created = adminWordPuzzleService.createPuzzle(
            title = request.title,
            themeCode = request.themeCode,
            difficultyCode = request.difficultyCode,
            boardWidth = request.boardWidth,
            boardHeight = request.boardHeight,
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(PuzzleItem.from(created))
    }

    // ── A-4. 퍼즐 수정 ──

    @PutMapping("/{id}")
    fun updatePuzzle(@PathVariable id: Long, @RequestBody request: AdminWordPuzzleRequest): ResponseEntity<PuzzleItem> {
        val updated = adminWordPuzzleService.updatePuzzle(
            id = id,
            title = request.title,
            themeCode = request.themeCode,
            difficultyCode = request.difficultyCode,
            boardWidth = request.boardWidth,
            boardHeight = request.boardHeight,
        )
        return ResponseEntity.ok(PuzzleItem.from(updated))
    }

    // ── A-5. 퍼즐 상태 변경 ──

    @PatchMapping("/{id}/status")
    fun changeStatus(@PathVariable id: Long, @RequestBody request: AdminWordPuzzleStatusRequest): ResponseEntity<PuzzleItem> {
        val updated = adminWordPuzzleService.changeStatus(id, request.status)
        return ResponseEntity.ok(PuzzleItem.from(updated))
    }

    // ── A-6. 퍼즐 삭제 ──

    @DeleteMapping("/{id}")
    fun deletePuzzle(@PathVariable id: Long): ResponseEntity<Void> {
        adminWordPuzzleService.deletePuzzle(id)
        return ResponseEntity.noContent().build()
    }

    // ── A-7. 퍼즐 단서 목록 조회 ──

    @GetMapping("/{puzzleId}/entries")
    fun listEntries(
        @PathVariable puzzleId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ): ResponseEntity<AdminPageResponse<EntryItem>> {
        val pageable = PageRequest.of(page, size)
        val result = adminWordPuzzleService.findAllEntries(puzzleId, pageable)
        return ResponseEntity.ok(AdminPageResponse.from(result) { EntryItem.from(it) })
    }

    // ── A-8. 퍼즐 단서 단건 조회 ──

    @GetMapping("/{puzzleId}/entries/{entryId}")
    fun getEntry(@PathVariable puzzleId: Long, @PathVariable entryId: Long): ResponseEntity<EntryDetailItem> =
        ResponseEntity.ok(EntryDetailItem.from(adminWordPuzzleService.findEntryById(puzzleId, entryId)))

    // ── A-9. 퍼즐 단서 등록 ──

    @PostMapping("/{puzzleId}/entries")
    fun createEntry(
        @PathVariable puzzleId: Long,
        @RequestBody request: AdminWordPuzzleEntryRequest,
    ): ResponseEntity<EntryDetailItem> {
        val created = adminWordPuzzleService.createEntry(
            puzzleId = puzzleId,
            dictionaryId = request.dictionaryId,
            answerText = request.answerText,
            directionCode = request.directionCode,
            startRow = request.startRow,
            startCol = request.startCol,
            clueNumber = request.clueNumber,
            clueTypeCode = request.clueTypeCode,
            clueText = request.clueText,
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(EntryDetailItem.from(created))
    }

    // ── A-10. 퍼즐 단서 수정 ──

    @PutMapping("/{puzzleId}/entries/{entryId}")
    fun updateEntry(
        @PathVariable puzzleId: Long,
        @PathVariable entryId: Long,
        @RequestBody request: AdminWordPuzzleEntryRequest,
    ): ResponseEntity<EntryDetailItem> {
        val updated = adminWordPuzzleService.updateEntry(
            puzzleId = puzzleId,
            entryId = entryId,
            dictionaryId = request.dictionaryId,
            answerText = request.answerText,
            directionCode = request.directionCode,
            startRow = request.startRow,
            startCol = request.startCol,
            clueNumber = request.clueNumber,
            clueTypeCode = request.clueTypeCode,
            clueText = request.clueText,
        )
        return ResponseEntity.ok(EntryDetailItem.from(updated))
    }

    // ── A-11. 퍼즐 단서 삭제 ──

    @DeleteMapping("/{puzzleId}/entries/{entryId}")
    fun deleteEntry(@PathVariable puzzleId: Long, @PathVariable entryId: Long): ResponseEntity<Void> {
        adminWordPuzzleService.deleteEntry(puzzleId, entryId)
        return ResponseEntity.noContent().build()
    }

    // ── A-12. 사전 검색 ──

    @GetMapping("/dictionaries")
    fun searchDictionaries(
        @RequestParam term: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<AdminPageResponse<DictionarySearchItem>> {
        val pageable = PageRequest.of(page, size)
        val result = adminWordPuzzleService.searchDictionaries(term, pageable)
        return ResponseEntity.ok(AdminPageResponse.from(result) { DictionarySearchItem.from(it) })
    }

    // ── Response DTOs ──

    data class PuzzleItem(
        val id: Long,
        val title: String,
        val themeCode: String,
        val difficultyCode: String,
        val boardWidth: Int,
        val boardHeight: Int,
        val puzzleStatusCode: String,
        val publishedAt: Instant?,
        val createdAt: Instant,
        val updatedAt: Instant,
    ) {
        companion object {
            fun from(p: WordPuzzle) = PuzzleItem(
                id = p.id!!,
                title = p.title,
                themeCode = p.themeCode,
                difficultyCode = p.difficultyCode.name,
                boardWidth = p.boardWidth,
                boardHeight = p.boardHeight,
                puzzleStatusCode = p.puzzleStatusCode.name,
                publishedAt = p.publishedAt,
                createdAt = p.createdAt,
                updatedAt = p.updatedAt,
            )
        }
    }

    data class EntryItem(
        val id: Long,
        val clueNumber: Int,
        val directionCode: String,
        val dictionaryId: Long,
        val dictionaryTerm: String,
        val answerText: String,
        val startRow: Int,
        val startCol: Int,
        val length: Int,
        val clueTypeCode: String,
        val clueText: String,
        val createdAt: Instant,
    ) {
        companion object {
            fun from(e: WordPuzzleEntry) = EntryItem(
                id = e.id!!,
                clueNumber = e.clueNumber,
                directionCode = e.directionCode.name,
                dictionaryId = e.dictionary.id!!,
                dictionaryTerm = e.dictionary.term,
                answerText = e.answerText,
                startRow = e.startRow,
                startCol = e.startCol,
                length = e.length,
                clueTypeCode = e.clueTypeCode.name,
                clueText = e.clueText,
                createdAt = e.createdAt,
            )
        }
    }

    data class EntryDetailItem(
        val id: Long,
        val clueNumber: Int,
        val directionCode: String,
        val dictionaryId: Long,
        val dictionaryTerm: String,
        val dictionaryDescription: String?,
        val answerText: String,
        val startRow: Int,
        val startCol: Int,
        val length: Int,
        val clueTypeCode: String,
        val clueText: String,
        val createdAt: Instant,
    ) {
        companion object {
            fun from(e: WordPuzzleEntry) = EntryDetailItem(
                id = e.id!!,
                clueNumber = e.clueNumber,
                directionCode = e.directionCode.name,
                dictionaryId = e.dictionary.id!!,
                dictionaryTerm = e.dictionary.term,
                dictionaryDescription = e.dictionary.description,
                answerText = e.answerText,
                startRow = e.startRow,
                startCol = e.startCol,
                length = e.length,
                clueTypeCode = e.clueTypeCode.name,
                clueText = e.clueText,
                createdAt = e.createdAt,
            )
        }
    }

    data class DictionarySearchItem(
        val id: Long,
        val term: String,
        val description: String?,
        val originalLanguageCode: String?,
        val originalLexeme: String?,
    ) {
        companion object {
            fun from(d: Dictionary) = DictionarySearchItem(
                id = d.id!!,
                term = d.term,
                description = d.description,
                originalLanguageCode = d.originalLanguageCode?.name,
                originalLexeme = d.originalLexeme,
            )
        }
    }
}
