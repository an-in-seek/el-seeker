package com.elseeker.bible.adapter.input.api.response

import com.elseeker.bible.domain.model.BibleVerseHighlight
import java.time.Instant

class BibleHighlightApiResponse {

    data class HighlightItem(
        val highlightId: Long,
        val verseNumber: Int,
        val color: String,
        val updatedAt: Instant?
    ) {
        companion object {
            fun from(highlight: BibleVerseHighlight) = HighlightItem(
                highlightId = highlight.id ?: 0,
                verseNumber = highlight.verseNumber,
                color = highlight.color.id,
                updatedAt = highlight.updatedAt
            )
        }
    }
}
