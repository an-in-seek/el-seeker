package com.elseeker.bible.presentation.web.study.response

import com.elseeker.bible.domain.study.model.Dictionary

object DictionaryViewResponse {

    data class ListItem(
        val id: Long,
        val term: String,
        val summary: String
    ) {
        companion object {
            fun from(entity: Dictionary) =
                ListItem(
                    id = entity.id!!,
                    term = entity.term,
                    summary = createSummary(entity.description)
                )

            private fun createSummary(description: String?): String {
                val normalized = description?.trim().orEmpty()
                if (normalized.isBlank()) {
                    return "설명이 등록되지 않았습니다."
                }
                return if (normalized.length <= 120) {
                    normalized
                } else {
                    normalized.substring(0, 120).trimEnd() + "..."
                }
            }
        }
    }

    data class Detail(
        val id: Long,
        val term: String,
        val description: String,
        val relatedVerses: String
    ) {
        companion object {
            fun from(entity: Dictionary) =
                Detail(
                    id = entity.id!!,
                    term = entity.term,
                    description = entity.description?.trim().orEmpty(),
                    relatedVerses = entity.relatedVerses?.trim().orEmpty()
                )
        }
    }
}