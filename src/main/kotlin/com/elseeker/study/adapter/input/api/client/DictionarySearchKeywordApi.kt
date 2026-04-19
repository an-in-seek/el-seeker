package com.elseeker.study.adapter.input.api.client

import com.elseeker.study.adapter.input.api.client.response.DictionarySearchKeywordRankingResponse
import com.elseeker.study.application.service.DictionarySearchKeywordService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/study/dictionaries/search-keywords")
class DictionarySearchKeywordApi(
    private val dictionarySearchKeywordService: DictionarySearchKeywordService,
) : DictionarySearchKeywordApiDocument {

    @GetMapping("/ranking")
    override fun getRanking(
        @RequestParam(defaultValue = "10") limit: Int,
    ): ResponseEntity<DictionarySearchKeywordRankingResponse> {
        val results = dictionarySearchKeywordService.getRanking(limit)
        return ResponseEntity.ok(DictionarySearchKeywordRankingResponse.from(results))
    }
}
