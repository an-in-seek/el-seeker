package com.elseeker.study.domain.vo

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("NormalizedDictionaryKeyword 단위테스트")
class NormalizedDictionaryKeywordTest {

    @Nested
    @DisplayName("ofOrNull_메서드는")
    inner class ofOrNull_메서드는 {

        @Test
        fun `양끝 공백을 제거한다`() {
            val keyword = NormalizedDictionaryKeyword.ofOrNull("  바울  ").shouldNotBeNull()
            keyword.value shouldBe "바울"
        }

        @Test
        fun `연속된 공백을 단일 공백으로 압축한다`() {
            val keyword = NormalizedDictionaryKeyword.ofOrNull("예수   그리스도").shouldNotBeNull()
            keyword.value shouldBe "예수 그리스도"
        }

        @Test
        fun `영문 대소문자를 소문자로 정규화한다`() {
            val keyword = NormalizedDictionaryKeyword.ofOrNull("LOVE").shouldNotBeNull()
            keyword.value shouldBe "love"
        }

        @Test
        fun `정규화 후 빈 문자열이면 null 을 반환한다`() {
            NormalizedDictionaryKeyword.ofOrNull(" ").shouldBeNull()
        }

        @Test
        fun `정규화 후 50자 초과면 null 을 반환한다`() {
            val tooLong = "a".repeat(51)
            NormalizedDictionaryKeyword.ofOrNull(tooLong).shouldBeNull()
        }

        @Test
        fun `정규화 후 정확히 1자도 정상 반환한다`() {
            val keyword = NormalizedDictionaryKeyword.ofOrNull(" 욥 ").shouldNotBeNull()
            keyword.value shouldBe "욥"
            keyword.isSingleChar() shouldBe true
        }
    }
}
