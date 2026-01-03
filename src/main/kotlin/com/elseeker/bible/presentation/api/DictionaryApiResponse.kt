package com.elseeker.bible.presentation.api

import com.elseeker.bible.domain.bible.model.BibleDictionary

object DictionaryApiResponse {

    data class DictionaryItem(
        val id: Long,
        val term: String,
        val description: String?,
        val relatedVerses: String?
    ) {
        companion object {
            fun from(entity: BibleDictionary) =
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
            fun from(entity: BibleDictionary) =
                DictionaryDetail(
                    id = entity.id!!,
                    term = entity.term,
                    description = entity.description,
                    relatedVerses = entity.relatedVerses
                )
        }
    }
}
