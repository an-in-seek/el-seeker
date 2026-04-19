package com.elseeker.study.adapter.output.jpa

import com.elseeker.study.application.result.DictionarySearchKeywordRankingResult
import com.elseeker.study.domain.model.DictionarySearchKeyword
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface DictionarySearchKeywordRepository : JpaRepository<DictionarySearchKeyword, Long> {

    @Modifying
    @Query(
        value = """
            INSERT INTO dictionary_search_keyword
                (normalized_keyword, keyword, search_count, blocked, last_searched_at, created_at, updated_at)
            VALUES
                (:normalized, :original, 1, false, :now, :now, :now)
            ON CONFLICT (normalized_keyword)
            DO UPDATE SET
                search_count     = dictionary_search_keyword.search_count + 1,
                keyword          = EXCLUDED.keyword,
                last_searched_at = EXCLUDED.last_searched_at,
                updated_at       = EXCLUDED.updated_at
        """,
        nativeQuery = true
    )
    fun upsertCount(
        @Param("normalized") normalized: String,
        @Param("original") original: String,
        @Param("now") now: Instant,
    ): Int

    @Query(
        """
        SELECT new com.elseeker.study.application.result.DictionarySearchKeywordRankingResult(
            e.keyword, e.searchCount
        )
        FROM DictionarySearchKeyword e
        WHERE e.blocked = false
        ORDER BY e.searchCount DESC, e.lastSearchedAt DESC
        """
    )
    fun findTopRanking(pageable: Pageable): List<DictionarySearchKeywordRankingResult>
}
