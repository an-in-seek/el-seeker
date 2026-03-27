package com.elseeker.study.adapter.input.api.admin.request

data class AdminDictionaryRequest(
    val term: String,
    val description: String? = null,
)
