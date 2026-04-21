package com.elseeker.bible.domain.result

import com.elseeker.bible.domain.model.BibleBookMemo
import java.time.Instant

object BibleBookMemoResult {

    data class BookMemoSlice(
        val content: List<BookMemoItem>,
        val hasNext: Boolean,
        val size: Int,
        val number: Int,
        val totalCount: Long?
    )

    data class BookMemoItem(
        val bookMemoId: Long,
        val translationId: Long,
        val bookOrder: Int,
        val bookName: String,
        val content: String,
        val updatedAt: Instant
    ) {
        companion object {
            fun from(memo: BibleBookMemo, bookName: String) = BookMemoItem(
                bookMemoId = memo.id ?: 0,
                translationId = memo.translationId,
                bookOrder = memo.bookOrder,
                bookName = bookName,
                content = memo.content,
                updatedAt = memo.updatedAt
            )
        }
    }
}
