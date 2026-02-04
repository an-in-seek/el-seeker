package com.elseeker.bible.adapter.input.api.admin.request

import com.elseeker.bible.domain.vo.BibleTranslationType
import com.neovisionaries.i18n.LanguageCode

data class AdminBibleTranslationRequest(
    val translationType: BibleTranslationType,
    val name: String,
    val translationOrder: Int,
    val languageCode: LanguageCode,
)
