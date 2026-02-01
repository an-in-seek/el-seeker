package com.elseeker.bible.adapter.input.api.admin

import com.elseeker.bible.adapter.input.api.admin.request.AdminBibleVerseRequest
import com.elseeker.bible.application.service.AdminBibleVerseService
import com.elseeker.bible.domain.model.BibleVerse
import com.elseeker.common.adapter.input.api.response.AdminPageResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/bible/chapters/{chapterId}/verses")
class AdminBibleVerseApi(
    private val adminBibleVerseService: AdminBibleVerseService,
) {

    @GetMapping
    fun list(
        @PathVariable chapterId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ): ResponseEntity<AdminPageResponse<VerseItem>> {
        val pageable = PageRequest.of(page, size, Sort.by("verseNumber"))
        val result = adminBibleVerseService.findByChapterId(chapterId, pageable)
        return ResponseEntity.ok(AdminPageResponse.from(result) { VerseItem.from(it) })
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<VerseItem> =
        ResponseEntity.ok(VerseItem.from(adminBibleVerseService.findById(id)))

    @PostMapping
    fun create(@PathVariable chapterId: Long, @RequestBody request: AdminBibleVerseRequest): ResponseEntity<VerseItem> {
        val created = adminBibleVerseService.create(chapterId, request.verseNumber, request.text)
        return ResponseEntity.ok(VerseItem.from(created))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: AdminBibleVerseRequest): ResponseEntity<VerseItem> {
        val updated = adminBibleVerseService.update(id, request.verseNumber, request.text)
        return ResponseEntity.ok(VerseItem.from(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        adminBibleVerseService.delete(id)
        return ResponseEntity.noContent().build()
    }

    data class VerseItem(
        val id: Long,
        val chapterId: Long,
        val verseNumber: Int,
        val text: String,
    ) {
        companion object {
            fun from(v: BibleVerse) = VerseItem(id = v.id!!, chapterId = v.chapterId, verseNumber = v.verseNumber, text = v.text)
        }
    }
}
