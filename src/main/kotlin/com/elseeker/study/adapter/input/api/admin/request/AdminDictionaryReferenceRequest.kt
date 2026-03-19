package com.elseeker.study.adapter.input.api.admin.request

data class AdminDictionaryReferenceRequest(
    val bookOrder: Int,
    val chapterNumber: Int,
    val verseNumber: Int,
    val verseLabel: String,
    val displayOrder: Int = 0
)

data class AdminDictionaryReferenceOrderRequest(
    val referenceIds: List<Long>
)
