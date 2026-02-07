package com.elseeker.study.adapter.input.api.client.response

import com.elseeker.study.domain.model.Dictionary

object DictionaryApiResponse {

    data class DictionaryItem(
        val id: Long,
        val term: String,
        val description: String?,
        val relatedVerses: String?
    ) {
        companion object {
            fun from(entity: Dictionary) =
                DictionaryItem(
                    id = entity.id!!,
                    term = entity.term,
                    description = entity.description,
                    relatedVerses = entity.relatedVerses
                )
        }
    }

    data class DictionarySliceResponse(
        val content: List<DictionaryItem>,
        val hasNext: Boolean,
        val totalCount: Long?
    )

    data class DictionaryDetail(
        val id: Long,
        val term: String,
        val description: String?,
        val relatedVerses: String?
    ) {
        companion object {
            fun from(entity: Dictionary) =
                DictionaryDetail(
                    id = entity.id!!,
                    term = entity.term,
                    description = entity.description,
                    relatedVerses = entity.relatedVerses
                )
        }
    }
}