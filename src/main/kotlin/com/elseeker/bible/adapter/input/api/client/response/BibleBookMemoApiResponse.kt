package com.elseeker.bible.adapter.input.api.client.response

import com.elseeker.bible.domain.model.BibleBookMemo
import java.time.Instant

class BibleBookMemoApiResponse {

    data class BookMemoItem(
        val bookMemoId: Long,
        val content: String,
        val updatedAt: Instant
    ) {
        companion object {
            fun from(memo: BibleBookMemo) = BookMemoItem(
                bookMemoId = memo.id ?: 0,
                content = memo.content,
                updatedAt = memo.updatedAt
            )
        }
    }
}
