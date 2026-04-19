package com.elseeker.bible.domain.vo

@JvmInline
value class NormalizedKeyword private constructor(val value: String) {

    companion object {

        private const val MIN_LENGTH = 2
        private const val MAX_LENGTH = 50
        private val WHITESPACE_REGEX = Regex("\\s+")

        fun ofOrNull(raw: String): NormalizedKeyword? {
            val collapsed = raw.trim().replace(WHITESPACE_REGEX, " ").lowercase()
            if (collapsed.length !in MIN_LENGTH..MAX_LENGTH) return null
            return NormalizedKeyword(collapsed)
        }
    }
}
