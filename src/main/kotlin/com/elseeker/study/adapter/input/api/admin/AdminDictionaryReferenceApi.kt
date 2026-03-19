package com.elseeker.study.adapter.input.api.admin

import com.elseeker.study.adapter.input.api.admin.request.AdminDictionaryReferenceOrderRequest
import com.elseeker.study.adapter.input.api.admin.request.AdminDictionaryReferenceRequest
import com.elseeker.study.application.service.AdminDictionaryReferenceService
import com.elseeker.study.domain.model.DictionaryReference
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/dictionaries/{dictionaryId}/references")
class AdminDictionaryReferenceApi(
    private val adminDictionaryReferenceService: AdminDictionaryReferenceService
) {

    @GetMapping
    fun list(@PathVariable dictionaryId: Long): ResponseEntity<List<ReferenceItem>> {
        val references = adminDictionaryReferenceService.findAllByDictionaryId(dictionaryId)
        return ResponseEntity.ok(references.map { ReferenceItem.from(it) })
    }

    @PostMapping
    fun create(
        @PathVariable dictionaryId: Long,
        @RequestBody request: AdminDictionaryReferenceRequest
    ): ResponseEntity<ReferenceItem> {
        val created = adminDictionaryReferenceService.create(
            dictionaryId = dictionaryId,
            bookOrder = request.bookOrder,
            chapterNumber = request.chapterNumber,
            verseNumber = request.verseNumber,
            verseLabel = request.verseLabel,
            displayOrder = request.displayOrder
        )
        return ResponseEntity.ok(ReferenceItem.from(created))
    }

    @PutMapping("/{refId}")
    fun update(
        @PathVariable dictionaryId: Long,
        @PathVariable refId: Long,
        @RequestBody request: AdminDictionaryReferenceRequest
    ): ResponseEntity<ReferenceItem> {
        val updated = adminDictionaryReferenceService.update(
            dictionaryId = dictionaryId,
            referenceId = refId,
            bookOrder = request.bookOrder,
            chapterNumber = request.chapterNumber,
            verseNumber = request.verseNumber,
            verseLabel = request.verseLabel,
            displayOrder = request.displayOrder
        )
        return ResponseEntity.ok(ReferenceItem.from(updated))
    }

    @DeleteMapping("/{refId}")
    fun delete(
        @PathVariable dictionaryId: Long,
        @PathVariable refId: Long
    ): ResponseEntity<Void> {
        adminDictionaryReferenceService.delete(dictionaryId, refId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/order")
    fun updateOrder(
        @PathVariable dictionaryId: Long,
        @RequestBody request: AdminDictionaryReferenceOrderRequest
    ): ResponseEntity<List<ReferenceItem>> {
        adminDictionaryReferenceService.updateOrder(dictionaryId, request.referenceIds)
        val references = adminDictionaryReferenceService.findAllByDictionaryId(dictionaryId)
        return ResponseEntity.ok(references.map { ReferenceItem.from(it) })
    }

    data class ReferenceItem(
        val referenceId: Long,
        val bookOrder: Int,
        val chapterNumber: Int,
        val verseNumber: Int,
        val verseLabel: String,
        val displayOrder: Int
    ) {
        companion object {
            fun from(ref: DictionaryReference) = ReferenceItem(
                referenceId = ref.id!!,
                bookOrder = ref.bookOrder,
                chapterNumber = ref.chapterNumber,
                verseNumber = ref.verseNumber,
                verseLabel = ref.verseLabel,
                displayOrder = ref.displayOrder
            )
        }
    }
}
