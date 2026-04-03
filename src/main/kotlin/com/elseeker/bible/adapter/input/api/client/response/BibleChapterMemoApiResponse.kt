package com.elseeker.bible.adapter.input.api.client.response

import com.elseeker.bible.domain.model.BibleChapterMemo
import java.time.Instant

class BibleChapterMemoApiResponse {

    data class ChapterMemoItem(
        val chapterMemoId: Long,
        val content: String,
        val updatedAt: Instant
    ) {
        companion object {
            fun from(memo: BibleChapterMemo) = ChapterMemoItem(
                chapterMemoId = memo.id ?: 0,
                content = memo.content,
                updatedAt = memo.updatedAt
            )
        }
    }
}
