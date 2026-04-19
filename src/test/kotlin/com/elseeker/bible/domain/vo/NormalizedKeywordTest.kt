package com.elseeker.bible.domain.vo

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("NormalizedKeyword 단위테스트")
class NormalizedKeywordTest {

    @Nested
    @DisplayName("ofOrNull_메서드는")
    inner class ofOrNull_메서드는 {

        @Test
        fun `양끝 공백을 제거한다`() {
            val keyword = NormalizedKeyword.ofOrNull("  사랑  ").shouldNotBeNull()
            keyword.value shouldBe "사랑"
        }

        @Test
        fun `연속된 공백을 단일 공백으로 압축한다`() {
            val keyword = NormalizedKeyword.ofOrNull("예수   그리스도").shouldNotBeNull()
            keyword.value shouldBe "예수 그리스도"
        }

        @Test
        fun `영문 대소문자를 소문자로 정규화한다`() {
            val keyword = NormalizedKeyword.ofOrNull("LOVE").shouldNotBeNull()
            keyword.value shouldBe "love"
        }

        @Test
        fun `정규화 후 2자 미만이면 null 을 반환한다`() {
            NormalizedKeyword.ofOrNull(" a ").shouldBeNull()
            NormalizedKeyword.ofOrNull("").shouldBeNull()
        }

        @Test
        fun `정규화 후 50자 초과면 null 을 반환한다`() {
            val tooLong = "a".repeat(51)
            NormalizedKeyword.ofOrNull(tooLong).shouldBeNull()
        }

        @Test
        fun `정규화 후 정확히 50자면 정상 반환한다`() {
            val exactly50 = "a".repeat(50)
            val keyword = NormalizedKeyword.ofOrNull(exactly50).shouldNotBeNull()
            keyword.value shouldBe exactly50
        }

        @Test
        fun `혼합 케이스 공백과 대소문자를 모두 정규화한다`() {
            val keyword = NormalizedKeyword.ofOrNull("  God  Is  LOVE  ").shouldNotBeNull()
            keyword.value shouldBe "god is love"
        }
    }
}
