package com.elseeker.study.application.service

import com.elseeker.common.IntegrationTest
import com.elseeker.common.domain.ServiceError
import com.elseeker.study.adapter.output.jpa.DictionaryRepository
import com.elseeker.study.adapter.output.jpa.DictionarySearchKeywordRepository
import com.elseeker.study.domain.event.DictionarySearchPerformedEvent
import com.elseeker.study.domain.model.Dictionary
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.support.TransactionTemplate

@DisplayName("DictionarySearchKeywordService 통합테스트")
class DictionarySearchKeywordServiceTest @Autowired constructor(
    private val dictionarySearchKeywordService: DictionarySearchKeywordService,
    private val dictionarySearchKeywordRepository: DictionarySearchKeywordRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val cacheManager: CacheManager,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val transactionTemplate: TransactionTemplate,
) : IntegrationTest() {

    @BeforeEach
    fun seedDictionaries() {
        dictionaryRepository.save(Dictionary(term = "바울", description = "사도 바울"))
        dictionaryRepository.save(Dictionary(term = "욥", description = "욥기 인물"))
        dictionaryRepository.save(Dictionary(term = "은혜", description = "은혜 설명"))
    }

    @Nested
    @DisplayName("increment_메서드는")
    inner class increment_메서드는 {

        @Test
        fun `같은 키워드로 여러 번 증가시키면 검색 횟수가 누적된다`() {
            repeat(5) { dictionarySearchKeywordService.increment("바울") }

            val all = dictionarySearchKeywordRepository.findAll()
            all shouldHaveSize 1
            all[0].searchCount shouldBe 5L
            all[0].normalizedKeyword shouldBe "바울"
        }

        @Test
        fun `공백과 대소문자가 다른 키워드도 정규화 후 동일 row로 병합된다`() {
            repeat(3) { dictionarySearchKeywordService.increment("  LOVE  ") }

            val all = dictionarySearchKeywordRepository.findAll()
            all shouldHaveSize 1
            all[0].searchCount shouldBe 3L
            all[0].normalizedKeyword shouldBe "love"
        }

        @Test
        fun `빈 키워드는 집계되지 않는다`() {
            dictionarySearchKeywordService.increment(" ")

            dictionarySearchKeywordRepository.findAll() shouldHaveSize 0
        }

        @Test
        fun `1글자 키워드는 정확한 표제어와 일치할 때만 집계된다`() {
            dictionarySearchKeywordService.increment("욥")
            dictionarySearchKeywordService.increment("김")

            val all = dictionarySearchKeywordRepository.findAll()
            all shouldHaveSize 1
            all[0].normalizedKeyword shouldBe "욥"
            all[0].searchCount shouldBe 1L
        }

        @Test
        fun `원본 키워드가 컬럼 길이를 초과해도 truncate 되어 저장된다`() {
            val longRaw = "ab" + "  ".repeat(26) + "cd"

            dictionarySearchKeywordService.increment(longRaw)

            val saved = dictionarySearchKeywordRepository.findAll().single()
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
            repeat(5) { dictionarySearchKeywordService.increment("바울") }
            repeat(3) { dictionarySearchKeywordService.increment("은혜") }
            repeat(1) { dictionarySearchKeywordService.increment("욥") }

            val ranking = dictionarySearchKeywordService.getRanking(2)
            ranking shouldHaveSize 2
            ranking[0].keyword shouldBe "바울"
            ranking[0].searchCount shouldBe 5L
            ranking[1].keyword shouldBe "은혜"
            ranking[1].searchCount shouldBe 3L
        }

        @Test
        fun `limit이 범위를 벗어나면 ServiceError를 던진다`() {
            clearRankingCache()
            shouldThrow<ServiceError> { dictionarySearchKeywordService.getRanking(0) }
            shouldThrow<ServiceError> { dictionarySearchKeywordService.getRanking(51) }
        }

        private fun clearRankingCache() {
            cacheManager.getCache("dictionary-search-keyword-ranking")?.clear()
        }
    }

    @Nested
    @DisplayName("이벤트_흐름은")
    inner class 이벤트_흐름은 {

        @Test
        fun `트랜잭션 커밋 이후 리스너가 실행되어 DB에 count가 반영된다`() {
            transactionTemplate.execute {
                applicationEventPublisher.publishEvent(DictionarySearchPerformedEvent("바울"))
            }

            val all = dictionarySearchKeywordRepository.findAll()
            all shouldHaveSize 1
            all[0].normalizedKeyword shouldBe "바울"
            all[0].searchCount shouldBe 1L
        }
    }
}
