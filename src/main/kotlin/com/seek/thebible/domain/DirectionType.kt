package com.seek.thebible.domain

enum class DirectionType {
    PREV, NEXT;

    companion object {
        fun fromString(value: String): DirectionType =
            entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid direction: $value")
    }
}
