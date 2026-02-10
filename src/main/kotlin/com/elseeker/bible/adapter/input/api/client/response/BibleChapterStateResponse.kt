package com.elseeker.bible.adapter.input.api.client.response

data class BibleChapterStateResponse(
    val memos: List<BibleMemoApiResponse.MemoItem>,
    val highlights: List<BibleHighlightApiResponse.HighlightItem>,
    val isRead: Boolean
)
