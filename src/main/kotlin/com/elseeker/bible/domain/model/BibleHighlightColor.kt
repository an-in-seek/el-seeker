package com.elseeker.bible.domain.model

enum class BibleHighlightColor(val id: String) {
    YELLOW("yellow"),
    GREEN("green"),
    PINK("pink");

    companion object {
        fun from(value: String): BibleHighlightColor {
            val normalized = value.trim().lowercase()
            return entries.firstOrNull { it.id == normalized }
                ?: throw IllegalArgumentException("invalid color: $value")
        }
    }
}
