package com.elseeker.bible.presentation.api

import com.elseeker.bible.application.bible.service.BibleDictionaryService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/study/dictionaries")
class BibleDictionaryApiController(
    private val bibleDictionaryService: BibleDictionaryService
) {

    @GetMapping
    fun getDictionaries(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<BibleDictionaryApiResponse.DictionarySliceResponse> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "term"))
        val dictionaryPage = bibleDictionaryService.getDictionaries(keyword, pageable)
        val response = BibleDictionaryApiResponse.DictionarySliceResponse(
            content = dictionaryPage.content.map(BibleDictionaryApiResponse.DictionaryItem::from),
            hasNext = dictionaryPage.hasNext(),
            totalCount = dictionaryPage.totalElements
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getDictionary(
        @PathVariable id: Long
    ): ResponseEntity<BibleDictionaryApiResponse.DictionaryDetail> {
        val response = BibleDictionaryApiResponse.DictionaryDetail.from(
            bibleDictionaryService.getDictionary(id)
        )
        return ResponseEntity.ok(response)
    }
}
