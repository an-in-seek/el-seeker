package com.elseeker.bible.presentation.api.study

import com.elseeker.bible.application.study.service.DictionaryService
import com.elseeker.bible.presentation.api.study.response.DictionaryApiResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/study/dictionaries")
class DictionaryApi(
    private val dictionaryService: DictionaryService
) : DictionaryApiDocument {

    @GetMapping
    fun getDictionaries(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<DictionaryApiResponse.DictionarySliceResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "term"))
        val dictionaryPage = dictionaryService.getDictionaries(keyword, pageable)
        val response = DictionaryApiResponse.DictionarySliceResponse(
            content = dictionaryPage.content.map(DictionaryApiResponse.DictionaryItem::from),
            hasNext = dictionaryPage.hasNext(),
            totalCount = dictionaryPage.totalElements
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getDictionary(
        @PathVariable id: Long
    ): ResponseEntity<DictionaryApiResponse.DictionaryDetail> {
        val response = DictionaryApiResponse.DictionaryDetail.from(
            dictionaryService.getDictionary(id)
        )
        return ResponseEntity.ok(response)
    }
}