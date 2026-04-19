package com.elseeker.bible.adapter.input.api.admin

import com.elseeker.bible.adapter.input.api.admin.response.AdminSearchKeywordRankingResponse
import com.elseeker.bible.application.service.BibleSearchKeywordService
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/bible/search-keywords")
class AdminBibleSearchKeywordApi(
    private val bibleSearchKeywordService: BibleSearchKeywordService,
) : AdminBibleSearchKeywordApiDocument {

    @GetMapping("/ranking")
    override fun getRanking(
        @RequestParam(defaultValue = "10") limit: Int,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<AdminSearchKeywordRankingResponse> {
        val results = bibleSearchKeywordService.getRanking(limit)
        return ResponseEntity.ok(AdminSearchKeywordRankingResponse.from(results))
    }
}
