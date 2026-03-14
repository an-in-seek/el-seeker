package com.elseeker.bible.adapter.input.api.client

import com.elseeker.bible.application.service.BibleMemoService
import com.elseeker.bible.domain.result.BibleMemoResult
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bibles/my-memos")
class BibleMyMemoApi(
    private val bibleMemoService: BibleMemoService
) {

    @GetMapping
    fun getMyMemos(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<BibleMemoResult.MemoSlice> {
        val effectiveSize = size.coerceIn(1, 100)
        val pageable = PageRequest.of(page, effectiveSize)
        val result = bibleMemoService.getMyMemos(principal.memberUid, pageable)
        return ResponseEntity.ok(result)
    }
}
