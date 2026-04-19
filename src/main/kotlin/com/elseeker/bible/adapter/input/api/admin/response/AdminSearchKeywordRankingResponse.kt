package com.elseeker.bible.adapter.input.api.admin.response

import com.elseeker.bible.application.result.SearchKeywordRankingResult
import java.time.Instant

data class AdminSearchKeywordRankingResponse(
    val items: List<Item>,
    val refreshedAt: Instant,
) {
    data class Item(
        val rank: Int,
        val keyword: String,
        val searchCount: Long,
    )

    companion object {
        fun from(results: List<SearchKeywordRankingResult>): AdminSearchKeywordRankingResponse {
            val items = results.mapIndexed { index, result ->
                Item(
                    rank = index + 1,
                    keyword = result.keyword,
                    searchCount = result.searchCount,
                )
            }
            return AdminSearchKeywordRankingResponse(
                items = items,
                refreshedAt = Instant.now(),
            )
        }
    }
}
