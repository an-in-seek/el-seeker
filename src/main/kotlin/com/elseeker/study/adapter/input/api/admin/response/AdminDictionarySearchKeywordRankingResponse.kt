package com.elseeker.study.adapter.input.api.admin.response

import com.elseeker.study.application.result.DictionarySearchKeywordRankingResult
import java.time.Instant

data class AdminDictionarySearchKeywordRankingResponse(
    val items: List<Item>,
    val refreshedAt: Instant,
) {
    data class Item(
        val rank: Int,
        val keyword: String,
        val searchCount: Long,
    )

    companion object {
        fun from(results: List<DictionarySearchKeywordRankingResult>): AdminDictionarySearchKeywordRankingResponse {
            val items = results.mapIndexed { index, result ->
                Item(
                    rank = index + 1,
                    keyword = result.keyword,
                    searchCount = result.searchCount,
                )
            }
            return AdminDictionarySearchKeywordRankingResponse(
                items = items,
                refreshedAt = Instant.now(),
            )
        }
    }
}
