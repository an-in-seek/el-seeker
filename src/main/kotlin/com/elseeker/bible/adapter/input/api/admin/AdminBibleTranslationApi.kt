package com.elseeker.bible.adapter.input.api.admin

import com.elseeker.bible.adapter.input.api.admin.request.AdminBibleTranslationRequest
import com.elseeker.bible.application.service.AdminBibleTranslationService
import com.elseeker.bible.domain.model.BibleTranslation
import com.elseeker.bible.domain.vo.BibleTranslationType
import com.elseeker.common.adapter.input.api.admin.response.AdminPageResponse
import com.neovisionaries.i18n.LanguageCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/bible/translations")
class AdminBibleTranslationApi(
    private val adminBibleTranslationService: AdminBibleTranslationService,
) {

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<AdminPageResponse<TranslationItem>> {
        val pageable = PageRequest.of(page, size, Sort.by("translationOrder"))
        val result = adminBibleTranslationService.findAll(pageable)
        return ResponseEntity.ok(AdminPageResponse.from(result) { TranslationItem.from(it) })
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<TranslationItem> =
        ResponseEntity.ok(TranslationItem.from(adminBibleTranslationService.findById(id)))

    @PostMapping
    fun create(@RequestBody request: AdminBibleTranslationRequest): ResponseEntity<TranslationItem> {
        val created = adminBibleTranslationService.create(request.translationType, request.name, request.translationOrder, request.languageCode)
        return ResponseEntity.ok(TranslationItem.from(created))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: AdminBibleTranslationRequest): ResponseEntity<TranslationItem> {
        val updated = adminBibleTranslationService.update(id, request.translationType, request.name, request.translationOrder, request.languageCode)
        return ResponseEntity.ok(TranslationItem.from(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        adminBibleTranslationService.delete(id)
        return ResponseEntity.noContent().build()
    }

    data class TranslationItem(
        val id: Long,
        val translationType: BibleTranslationType,
        val name: String,
        val translationOrder: Int,
        val languageCode: LanguageCode,
    ) {
        companion object {
            fun from(t: BibleTranslation) = TranslationItem(
                id = t.id!!,
                translationType = t.translationType,
                name = t.name,
                translationOrder = t.translationOrder,
                languageCode = t.languageCode,
            )
        }
    }
}
