package com.elseeker.bible.adapter.input.api.admin

import com.elseeker.bible.adapter.input.api.admin.request.AdminBibleChapterRequest
import com.elseeker.bible.application.service.AdminBibleChapterService
import com.elseeker.bible.domain.model.BibleChapter
import com.elseeker.common.adapter.input.api.response.AdminPageResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/bible/books/{bookId}/chapters")
class AdminBibleChapterApi(
    private val adminBibleChapterService: AdminBibleChapterService,
) {

    @GetMapping
    fun list(
        @PathVariable bookId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ): ResponseEntity<AdminPageResponse<ChapterItem>> {
        val pageable = PageRequest.of(page, size, Sort.by("chapterNumber"))
        val result = adminBibleChapterService.findByBookId(bookId, pageable)
        return ResponseEntity.ok(AdminPageResponse.from(result) { ChapterItem.from(it) })
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<ChapterItem> =
        ResponseEntity.ok(ChapterItem.from(adminBibleChapterService.findById(id)))

    @PostMapping
    fun create(@PathVariable bookId: Long, @RequestBody request: AdminBibleChapterRequest): ResponseEntity<ChapterItem> {
        val created = adminBibleChapterService.create(bookId, request.chapterNumber)
        return ResponseEntity.ok(ChapterItem.from(created))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: AdminBibleChapterRequest): ResponseEntity<ChapterItem> {
        val updated = adminBibleChapterService.update(id, request.chapterNumber)
        return ResponseEntity.ok(ChapterItem.from(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        adminBibleChapterService.delete(id)
        return ResponseEntity.noContent().build()
    }

    data class ChapterItem(
        val id: Long,
        val bookId: Long,
        val chapterNumber: Int,
    ) {
        companion object {
            fun from(c: BibleChapter) = ChapterItem(id = c.id!!, bookId = c.bookId, chapterNumber = c.chapterNumber)
        }
    }
}
