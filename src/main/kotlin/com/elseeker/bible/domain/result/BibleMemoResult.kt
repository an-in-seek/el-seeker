package com.elseeker.bible.domain.result

import com.elseeker.bible.domain.model.BibleVerseMemo
import java.time.Instant

object BibleMemoResult {

    data class MemoSlice(
        val content: List<MemoItem>,
        val hasNext: Boolean,
        val size: Int,
        val number: Int,
        val totalCount: Long?
    )

    data class MemoItem(
        val memoId: Long,
        val translationId: Long,
        val bookOrder: Int,
        val bookName: String,
        val chapterNumber: Int,
        val verseNumber: Int,
        val content: String,
        val updatedAt: Instant
    ) {
        companion object {
            fun from(memo: BibleVerseMemo, bookName: String) = MemoItem(
                memoId = memo.id ?: 0,
                translationId = memo.translationId,
                bookOrder = memo.bookOrder,
                bookName = bookName,
                chapterNumber = memo.chapterNumber,
                verseNumber = memo.verseNumber,
                content = memo.content,
                updatedAt = memo.updatedAt
            )
        }
    }
}
