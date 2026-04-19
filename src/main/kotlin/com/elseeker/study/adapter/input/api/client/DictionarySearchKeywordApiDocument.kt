package com.elseeker.study.adapter.input.api.client

import com.elseeker.study.adapter.input.api.client.response.DictionarySearchKeywordRankingResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "Dictionary Search Keyword", description = "성경 사전 검색 키워드 API")
interface DictionarySearchKeywordApiDocument {

    @Operation(
        summary = "성경 사전 인기 검색어 랭킹 조회",
        description = "성경 사전 검색의 인기 키워드 상위 N개를 조회합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터 (limit 범위 위반)"),
    )
    fun getRanking(
        @Parameter(description = "조회 개수 (1~50, 기본 10)") limit: Int,
    ): ResponseEntity<DictionarySearchKeywordRankingResponse>
}
