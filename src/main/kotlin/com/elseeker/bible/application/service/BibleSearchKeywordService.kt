package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleSearchKeywordRepository
import com.elseeker.bible.application.result.SearchKeywordRankingResult
import com.elseeker.bible.domain.vo.NormalizedKeyword
import com.elseeker.common.config.CacheConfig
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class BibleSearchKeywordService(
    private val bibleSearchKeywordRepository: BibleSearchKeywordRepository,
) {

    @Transactional
    fun increment(rawKeyword: String) {
        val normalized = NormalizedKeyword.ofOrNull(rawKeyword) ?: return
        val original = rawKeyword.trim().take(MAX_KEYWORD_LENGTH)
        bibleSearchKeywordRepository.upsertCount(
            normalized = normalized.value,
            original = original,
            now = Instant.now(),
        )
    }

    @Cacheable(
        value = [CacheConfig.CACHE_BIBLE_SEARCH_KEYWORD_RANKING],
        key = "#limit"
    )
    @Transactional(readOnly = true)
    fun getRanking(limit: Int): List<SearchKeywordRankingResult> {
        if (limit !in MIN_LIMIT..MAX_LIMIT) {
            throwError(ErrorType.INVALID_PARAMETER, "limit must be in $MIN_LIMIT..$MAX_LIMIT")
        }
        return bibleSearchKeywordRepository.findTopRanking(PageRequest.of(0, limit))
    }

    companion object {
        private const val MIN_LIMIT = 1
        private const val MAX_LIMIT = 50
        private const val MAX_KEYWORD_LENGTH = 50
    }
}
