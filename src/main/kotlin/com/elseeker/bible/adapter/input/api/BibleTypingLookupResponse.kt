package com.elseeker.bible.adapter.input.api

object BibleTypingLookupResponse {

    data class Translation(
        val id: Long,
        val name: String,
        val code: String
    )

    data class Book(
        val bookOrder: Int,
        val name: String
    )

    data class Chapter(
        val chapterNumber: Int
    )

    data class Verse(
        val verseNumber: Int,
        val text: String
    )
}
