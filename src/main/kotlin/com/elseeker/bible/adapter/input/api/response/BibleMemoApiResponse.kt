package com.elseeker.bible.adapter.input.api.response

import com.elseeker.bible.domain.model.BibleVerseMemo
import java.time.LocalDateTime

class BibleMemoApiResponse {

    data class MemoItem(
        val memoId: Long,
        val verseNumber: Int,
        val content: String,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            fun from(memo: BibleVerseMemo) = MemoItem(
                memoId = memo.id ?: 0,
                verseNumber = memo.verseNumber,
                content = memo.content,
                updatedAt = memo.updatedAt
            )
        }
    }
}
