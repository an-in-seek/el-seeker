package com.elseeker.bible.adapter.input.api.client.request

data class BibleReadingProgressRequest(
    val translationId: Long,
    val bookOrder: Int,
    val chapterNumber: Int
)
