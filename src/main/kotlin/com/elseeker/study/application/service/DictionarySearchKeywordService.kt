package com.elseeker.study.application.service

import com.elseeker.common.config.CacheConfig
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.study.adapter.output.jpa.DictionaryRepository
import com.elseeker.study.adapter.output.jpa.DictionarySearchKeywordRepository
import com.elseeker.study.application.result.DictionarySearchKeywordRankingResult
import com.elseeker.study.domain.vo.NormalizedDictionaryKeyword
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DictionarySearchKeywordService(
    private val dictionarySearchKeywordRepository: DictionarySearchKeywordRepository,
    private val dictionaryRepository: DictionaryRepository,
) {

    @Transactional
    fun increment(rawKeyword: String) {
        val normalized = NormalizedDictionaryKeyword.ofOrNull(rawKeyword) ?: return
        val original = rawKeyword.trim().take(MAX_KEYWORD_LENGTH)

        if (normalized.isSingleChar() && !dictionaryRepository.existsByExactTermIgnoreCase(original)) {
            return
        }

        dictionarySearchKeywordRepository.upsertCount(
            normalized = normalized.value,
            original = original,
            now = Instant.now(),
        )
    }

    @Cacheable(
        value = [CacheConfig.CACHE_DICTIONARY_SEARCH_KEYWORD_RANKING],
        key = "#limit"
    )
    @Transactional(readOnly = true)
    fun getRanking(limit: Int): List<DictionarySearchKeywordRankingResult> {
        if (limit !in MIN_LIMIT..MAX_LIMIT) {
            throwError(ErrorType.INVALID_PARAMETER, "limit must be in $MIN_LIMIT..$MAX_LIMIT")
        }
        return dictionarySearchKeywordRepository.findTopRanking(PageRequest.of(0, limit))
    }

    companion object {
        private const val MIN_LIMIT = 1
        private const val MAX_LIMIT = 50
        private const val MAX_KEYWORD_LENGTH = 50
    }
}
