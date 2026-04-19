package com.elseeker.study.adapter.input.api.admin

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.study.adapter.input.api.admin.response.AdminDictionarySearchKeywordRankingResponse
import com.elseeker.study.application.service.DictionarySearchKeywordService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/dictionaries/search-keywords")
class AdminDictionarySearchKeywordApi(
    private val dictionarySearchKeywordService: DictionarySearchKeywordService,
) {

    @GetMapping("/ranking")
    fun getRanking(
        @RequestParam(defaultValue = "10") limit: Int,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<AdminDictionarySearchKeywordRankingResponse> {
        val results = dictionarySearchKeywordService.getRanking(limit)
        return ResponseEntity.ok(AdminDictionarySearchKeywordRankingResponse.from(results))
    }
}
