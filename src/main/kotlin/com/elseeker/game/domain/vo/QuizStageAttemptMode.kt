package com.elseeker.game.domain.vo

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class QuizStageAttemptMode {
    RECORD,
    REVIEW;

    @JsonValue
    fun toValue(): String = name.lowercase()

    companion object {
        @JsonCreator
        @JvmStatic
        fun from(raw: String?): QuizStageAttemptMode {
            val normalized = raw?.trim()?.uppercase()
            return when (normalized) {
                "RECORD" -> RECORD
                "REVIEW" -> REVIEW
                else -> RECORD
            }
        }
    }
}
