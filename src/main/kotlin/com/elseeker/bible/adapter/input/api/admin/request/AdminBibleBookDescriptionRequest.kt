package com.elseeker.bible.adapter.input.api.admin.request

import com.elseeker.bible.domain.vo.BibleBookKey
import com.neovisionaries.i18n.LanguageCode

data class AdminBibleBookDescriptionRequest(
    val bookKey: BibleBookKey,
    val languageCode: LanguageCode,
    val summary: String,
    val author: String,
    val writtenYear: String,
    val historicalPeriod: String,
    val background: String,
    val content: String,
)
