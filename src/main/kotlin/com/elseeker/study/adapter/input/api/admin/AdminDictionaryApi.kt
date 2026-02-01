package com.elseeker.study.adapter.input.api.admin

import com.elseeker.common.adapter.input.api.response.AdminPageResponse
import com.elseeker.study.adapter.input.api.admin.request.AdminDictionaryRequest
import com.elseeker.study.application.service.AdminDictionaryService
import com.elseeker.study.domain.model.Dictionary
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/dictionaries")
class AdminDictionaryApi(
    private val adminDictionaryService: AdminDictionaryService,
) {

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) keyword: String?,
    ): ResponseEntity<AdminPageResponse<DictionaryItem>> {
        val pageable = PageRequest.of(page, size, Sort.by("term"))
        val result = adminDictionaryService.findAll(keyword, pageable)
        return ResponseEntity.ok(AdminPageResponse.from(result) { DictionaryItem.from(it) })
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<DictionaryItem> =
        ResponseEntity.ok(DictionaryItem.from(adminDictionaryService.findById(id)))

    @PostMapping
    fun create(@RequestBody request: AdminDictionaryRequest): ResponseEntity<DictionaryItem> {
        val created = adminDictionaryService.create(request.term, request.description, request.relatedVerses)
        return ResponseEntity.ok(DictionaryItem.from(created))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: AdminDictionaryRequest): ResponseEntity<DictionaryItem> {
        val updated = adminDictionaryService.update(id, request.term, request.description, request.relatedVerses)
        return ResponseEntity.ok(DictionaryItem.from(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        adminDictionaryService.delete(id)
        return ResponseEntity.noContent().build()
    }

    data class DictionaryItem(
        val id: Long,
        val term: String,
        val description: String?,
        val relatedVerses: String?,
    ) {
        companion object {
            fun from(d: Dictionary) = DictionaryItem(
                id = d.id!!,
                term = d.term,
                description = d.description,
                relatedVerses = d.relatedVerses,
            )
        }
    }
}
