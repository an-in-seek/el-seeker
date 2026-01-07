package com.elseeker.bible.presentation.api.bible.response

data class BibleSearchResponse(
    val bookId: Long,
    val bookOrder: Int,
    val bookName: String,
    val chapterId: Long,
    val chapterNumber: Int,
    val verseId: Long,
    val verseNumber: Int,
    val text: String
)

data class BibleSearchSliceResponse(
    val content: List<BibleSearchResponse>,
    val hasNext: Boolean,
    val totalCount: Long?
)
