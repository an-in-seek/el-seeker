package com.elseeker.bible.adapter.input.api.client.response

import com.elseeker.bible.application.result.SearchKeywordRankingResult
import java.time.Instant

data class BibleSearchKeywordRankingResponse(
    val items: List<Item>,
    val refreshedAt: Instant,
) {
    data class Item(
        val rank: Int,
        val keyword: String,
        val searchCount: Long,
    )

    companion object {
        fun from(results: List<SearchKeywordRankingResult>): BibleSearchKeywordRankingResponse {
            val items = results.mapIndexed { index, result ->
                Item(
                    rank = index + 1,
                    keyword = result.keyword,
                    searchCount = result.searchCount,
                )
            }
            return BibleSearchKeywordRankingResponse(
                items = items,
                refreshedAt = Instant.now(),
            )
        }
    }
}
