package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleSearchKeywordRepository
import com.elseeker.bible.domain.event.BibleSearchPerformedEvent
import com.elseeker.common.IntegrationTest
import com.elseeker.common.domain.ServiceError
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("BibleSearchKeywordService 통합테스트")
class BibleSearchKeywordServiceTest @Autowired constructor(
    private val bibleSearchKeywordService: BibleSearchKeywordService,
    private val bibleSearchKeywordRepository: BibleSearchKeywordRepository,
    private val cacheManager: CacheManager,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val transactionTemplate: TransactionTemplate,
) : IntegrationTest() {

    @Nested
    @DisplayName("increment_메서드는")
    inner class increment_메서드는 {

        @Test
        fun `같은 키워드로 여러 번 증가시키면 검색 횟수가 누적된다`() {
            repeat(5) { bibleSearchKeywordService.increment("사랑") }

            val all = bibleSearchKeywordRepository.findAll()
            all shouldHaveSize 1
            all[0].searchCount shouldBe 5L
            all[0].normalizedKeyword shouldBe "사랑"
        }

        @Test
        fun `공백과 대소문자가 다른 키워드도 정규화 후 동일 row로 병합된다`() {
            bibleSearchKeywordService.increment("Love")
            bibleSearchKeywordService.increment("  LOVE  ")
            bibleSearchKeywordService.increment("love")

            val all = bibleSearchKeywordRepository.findAll()
            all shouldHaveSize 1
            all[0].searchCount shouldBe 3L
            all[0].normalizedKeyword shouldBe "love"
        }

        @Test
        fun `2자 미만 키워드는 집계되지 않는다`() {
            bibleSearchKeywordService.increment("a")
            bibleSearchKeywordService.increment(" ")

            bibleSearchKeywordRepository.findAll() shouldHaveSize 0
        }

        @Test
        fun `서로 다른 정규화 키워드는 서로 다른 row로 저장된다`() {
            bibleSearchKeywordService.increment("사랑")
            bibleSearchKeywordService.increment("믿음")
            bibleSearchKeywordService.increment("사랑")

            val all = bibleSearchKeywordRepository.findAll()
                .associateBy { it.normalizedKeyword }
            all.size shouldBe 2
            all["사랑"].shouldNotBeNull().searchCount shouldBe 2L
            all["믿음"].shouldNotBeNull().searchCount shouldBe 1L
        }

        @Test
        fun `원본 키워드가 컬럼 길이를 초과해도 truncate 되어 DataException 없이 저장된다`() {
            // raw length: 2 + 52 + 2 = 56, trim 해도 동일
            // normalized: "ab" + " ".repeat(26) + "cd" = 30 chars (유효)
            // keyword 컬럼: rawKeyword.trim().take(50) 으로 50자로 잘림
            val longRaw = "ab" + "  ".repeat(26) + "cd"

            bibleSearchKeywordService.increment(longRaw)

            val saved = bibleSearchKeywordRepository.findAll().single()
            saved.keyword.length shouldBe 50
            saved.searchCount shouldBe 1L
        }
    }

    @Nested
    @DisplayName("getRanking_메서드는")
    inner class getRanking_메서드는 {

        @Test
        fun `검색 횟수 내림차순으로 상위 N개를 반환한다`() {
            clearRankingCache()
            repeat(5) { bibleSearchKeywordService.increment("사랑") }
            repeat(3) { bibleSearchKeywordService.increment("믿음") }
            repeat(1) { bibleSearchKeywordService.increment("소망") }

            val ranking = bibleSearchKeywordService.getRanking(2)
            ranking shouldHaveSize 2
            ranking[0].keyword shouldBe "사랑"
            ranking[0].searchCount shouldBe 5L
            ranking[1].keyword shouldBe "믿음"
            ranking[1].searchCount shouldBe 3L
        }

        @Test
        fun `limit이 범위를 벗어나면 ServiceError를 던진다`() {
            clearRankingCache()
            shouldThrow<ServiceError> { bibleSearchKeywordService.getRanking(0) }
            shouldThrow<ServiceError> { bibleSearchKeywordService.getRanking(51) }
        }

        private fun clearRankingCache() {
            cacheManager.getCache("bible-search-keyword-ranking")?.clear()
        }
    }

    @Nested
    @DisplayName("이벤트_흐름은")
    inner class 이벤트_흐름은 {

        @Test
        fun `트랜잭션 커밋 이후 리스너가 실행되어 DB에 count가 반영된다`() {
            transactionTemplate.execute {
                applicationEventPublisher.publishEvent(BibleSearchPerformedEvent("사랑"))
            }

            val all = bibleSearchKeywordRepository.findAll()
            all shouldHaveSize 1
            all[0].normalizedKeyword shouldBe "사랑"
            all[0].searchCount shouldBe 1L
        }

        @Test
        fun `여러 키워드 이벤트가 각각 정규화된 키로 누적된다`() {
            transactionTemplate.execute {
                applicationEventPublisher.publishEvent(BibleSearchPerformedEvent("Love"))
            }
            transactionTemplate.execute {
                applicationEventPublisher.publishEvent(BibleSearchPerformedEvent("LOVE"))
            }
            transactionTemplate.execute {
                applicationEventPublisher.publishEvent(BibleSearchPerformedEvent("믿음"))
            }

            val all = bibleSearchKeywordRepository.findAll()
                .associateBy { it.normalizedKeyword }
            all.size shouldBe 2
            all["love"].shouldNotBeNull().searchCount shouldBe 2L
            all["믿음"].shouldNotBeNull().searchCount shouldBe 1L
        }
    }
}
