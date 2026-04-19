package com.elseeker.study.application.service

import com.elseeker.study.adapter.output.jpa.DictionaryRepository
import com.elseeker.study.domain.event.DictionarySearchPerformedEvent
import com.elseeker.study.domain.model.Dictionary
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

@DisplayName("DictionaryService 단위테스트")
class DictionaryServiceTest {

    private val dictionaryRepository: DictionaryRepository = mock(DictionaryRepository::class.java)
    private val applicationEventPublisher: ApplicationEventPublisher = mock(ApplicationEventPublisher::class.java)
    private val dictionaryService = DictionaryService(dictionaryRepository, applicationEventPublisher)

    @Nested
    @DisplayName("getDictionaries_메서드는")
    inner class getDictionaries_메서드는 {

        @Test
        fun `첫 페이지이고 결과가 있으면 검색 이벤트를 발행한다`() {
            val pageable = PageRequest.of(0, 10)
            given(dictionaryRepository.findByTermContainingKo("바울", pageable))
                .willReturn(PageImpl(listOf(Dictionary(term = "바울")), pageable, 1))

            dictionaryService.getDictionaries("  바울  ", pageable)

            val captor = ArgumentCaptor.forClass(DictionarySearchPerformedEvent::class.java)
            verify(applicationEventPublisher).publishEvent(captor.capture())
            captor.value.keyword shouldBe "바울"
        }

        @Test
        fun `빈 검색어면 이벤트를 발행하지 않는다`() {
            val pageable = PageRequest.of(0, 10)
            given(dictionaryRepository.findAllOrderByKo(pageable))
                .willReturn(PageImpl(emptyList(), pageable, 0))

            dictionaryService.getDictionaries(" ", pageable)

            verify(applicationEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any())
        }

        @Test
        fun `두 번째 페이지 조회면 이벤트를 발행하지 않는다`() {
            val pageable = PageRequest.of(1, 10)
            given(dictionaryRepository.findByTermContainingKo("바울", pageable))
                .willReturn(PageImpl(listOf(Dictionary(term = "바울")), pageable, 1))

            dictionaryService.getDictionaries("바울", pageable)

            verify(applicationEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any())
        }

        @Test
        fun `검색 결과가 없으면 이벤트를 발행하지 않는다`() {
            val pageable = PageRequest.of(0, 10)
            given(dictionaryRepository.findByTermContainingKo("없는용어", pageable))
                .willReturn(PageImpl(emptyList(), pageable, 0))

            dictionaryService.getDictionaries("없는용어", pageable)

            verify(applicationEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any())
        }
    }
}
