package com.elseeker.bible.domain.result

import com.elseeker.bible.domain.model.BibleChapterMemo
import java.time.Instant

object BibleChapterMemoResult {

    data class ChapterMemoSlice(
        val content: List<ChapterMemoItem>,
        val hasNext: Boolean,
        val size: Int,
        val number: Int,
        val totalCount: Long?
    )

    data class ChapterMemoItem(
        val chapterMemoId: Long,
        val translationId: Long,
        val bookOrder: Int,
        val bookName: String,
        val chapterNumber: Int,
        val content: String,
        val updatedAt: Instant
    ) {
        companion object {
            fun from(memo: BibleChapterMemo, bookName: String) = ChapterMemoItem(
                chapterMemoId = memo.id ?: 0,
                translationId = memo.translationId,
                bookOrder = memo.bookOrder,
                bookName = bookName,
                chapterNumber = memo.chapterNumber,
                content = memo.content,
                updatedAt = memo.updatedAt
            )
        }
    }
}
