package com.elseeker.bible.adapter.input.api.admin

import com.elseeker.bible.adapter.input.api.admin.request.AdminBibleBookDescriptionRequest
import com.elseeker.bible.application.service.AdminBibleBookDescriptionService
import com.elseeker.bible.domain.model.BibleBookDescription
import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.common.adapter.input.api.admin.response.AdminPageResponse
import com.neovisionaries.i18n.LanguageCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/bible/book-descriptions")
class AdminBibleBookDescriptionApi(
    private val adminBibleBookDescriptionService: AdminBibleBookDescriptionService,
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) bookKey: BibleBookKey?,
        @RequestParam(required = false) languageCode: LanguageCode?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<AdminPageResponse<BookDescriptionItem>> {
        val pageable = PageRequest.of(page, size, Sort.by("bookKey", "languageCode"))
        val result = adminBibleBookDescriptionService.findAll(bookKey, languageCode, pageable)
        return ResponseEntity.ok(AdminPageResponse.from(result) { BookDescriptionItem.from(it) })
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<BookDescriptionItem> =
        ResponseEntity.ok(BookDescriptionItem.from(adminBibleBookDescriptionService.findById(id)))

    @PostMapping
    fun create(@RequestBody request: AdminBibleBookDescriptionRequest): ResponseEntity<BookDescriptionItem> {
        val created = adminBibleBookDescriptionService.create(
            bookKey = request.bookKey,
            languageCode = request.languageCode,
            summary = request.summary,
            author = request.author,
            writtenYear = request.writtenYear,
            historicalPeriod = request.historicalPeriod,
            background = request.background,
            content = request.content,
        )
        return ResponseEntity.ok(BookDescriptionItem.from(created))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: AdminBibleBookDescriptionRequest): ResponseEntity<BookDescriptionItem> {
        val updated = adminBibleBookDescriptionService.update(
            id = id,
            bookKey = request.bookKey,
            languageCode = request.languageCode,
            summary = request.summary,
            author = request.author,
            writtenYear = request.writtenYear,
            historicalPeriod = request.historicalPeriod,
            background = request.background,
            content = request.content,
        )
        return ResponseEntity.ok(BookDescriptionItem.from(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        adminBibleBookDescriptionService.delete(id)
        return ResponseEntity.noContent().build()
    }

    data class BookDescriptionItem(
        val id: Long,
        val bookKey: BibleBookKey,
        val languageCode: LanguageCode,
        val summary: String,
        val author: String,
        val writtenYear: String,
        val historicalPeriod: String,
        val background: String,
        val content: String,
    ) {
        companion object {
            fun from(entity: BibleBookDescription) = BookDescriptionItem(
                id = entity.id!!,
                bookKey = entity.bookKey,
                languageCode = entity.languageCode,
                summary = entity.summary,
                author = entity.author,
                writtenYear = entity.writtenYear,
                historicalPeriod = entity.historicalPeriod,
                background = entity.background,
                content = entity.content,
            )
        }
    }
}
