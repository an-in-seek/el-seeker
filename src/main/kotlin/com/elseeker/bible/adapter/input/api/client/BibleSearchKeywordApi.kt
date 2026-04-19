package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.adapter.input.api.client.response.BibleSearchKeywordRankingResponse
import com.elseeker.bible.application.service.BibleSearchKeywordService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bibles/search-keywords")
class BibleSearchKeywordApi(
    private val bibleSearchKeywordService: BibleSearchKeywordService,
) : BibleSearchKeywordApiDocument {

    @GetMapping("/ranking")
    override fun getRanking(
        @RequestParam(defaultValue = "10") limit: Int,
    ): ResponseEntity<BibleSearchKeywordRankingResponse> {
        val results = bibleSearchKeywordService.getRanking(limit)
        return ResponseEntity.ok(BibleSearchKeywordRankingResponse.from(results))
    }
}
