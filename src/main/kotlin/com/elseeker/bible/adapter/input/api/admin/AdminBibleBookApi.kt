package com.elseeker.bible.adapter.input.api.admin

import com.elseeker.bible.adapter.input.api.admin.request.AdminBibleBookRequest
import com.elseeker.bible.application.service.AdminBibleBookService
import com.elseeker.bible.domain.model.BibleBook
import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.bible.domain.vo.BibleTestamentType
import com.elseeker.common.adapter.input.api.admin.response.AdminPageResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/bible/translations/{translationId}/books")
class AdminBibleBookApi(
    private val adminBibleBookService: AdminBibleBookService,
) {

    @GetMapping
    fun list(
        @PathVariable translationId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<AdminPageResponse<BookItem>> {
        val pageable = PageRequest.of(page, size, Sort.by("bookOrder"))
        val result = adminBibleBookService.findByTranslationId(translationId, pageable)
        return ResponseEntity.ok(AdminPageResponse.from(result) { BookItem.from(it) })
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<BookItem> =
        ResponseEntity.ok(BookItem.from(adminBibleBookService.findById(id)))

    @PostMapping
    fun create(@PathVariable translationId: Long, @RequestBody request: AdminBibleBookRequest): ResponseEntity<BookItem> {
        val created = adminBibleBookService.create(translationId, request.bookKey, request.bookOrder, request.name, request.abbreviation, request.testamentType)
        return ResponseEntity.ok(BookItem.from(created))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: AdminBibleBookRequest): ResponseEntity<BookItem> {
        val updated = adminBibleBookService.update(id, request.bookKey, request.bookOrder, request.name, request.abbreviation, request.testamentType)
        return ResponseEntity.ok(BookItem.from(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        adminBibleBookService.delete(id)
        return ResponseEntity.noContent().build()
    }

    data class BookItem(
        val id: Long,
        val translationId: Long,
        val bookKey: BibleBookKey,
        val bookOrder: Int,
        val name: String,
        val abbreviation: String,
        val testamentType: BibleTestamentType,
    ) {
        companion object {
            fun from(b: BibleBook) = BookItem(
                id = b.id!!,
                translationId = b.translationId,
                bookKey = b.bookKey,
                bookOrder = b.bookOrder,
                name = b.name,
                abbreviation = b.abbreviation,
                testamentType = b.testamentType,
            )
        }
    }
}
