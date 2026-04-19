package com.elseeker.study.domain.vo

@JvmInline
value class NormalizedDictionaryKeyword private constructor(val value: String) {

    fun isSingleChar(): Boolean = value.length == 1

    companion object {
        private const val MIN_LENGTH = 1
        private const val MAX_LENGTH = 50
        private val WHITESPACE_REGEX = Regex("\\s+")

        fun ofOrNull(raw: String): NormalizedDictionaryKeyword? {
            val collapsed = raw.trim().replace(WHITESPACE_REGEX, " ").lowercase()
            if (collapsed.length !in MIN_LENGTH..MAX_LENGTH) return null
            return NormalizedDictionaryKeyword(collapsed)
        }
    }
}
