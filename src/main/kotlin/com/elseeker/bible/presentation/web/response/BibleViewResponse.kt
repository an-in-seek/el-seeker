package com.elseeker.bible.presentation.web.response

import com.elseeker.bible.domain.bible.model.BibleTranslationType
import com.elseeker.bible.domain.bible.model.LanguageCode
import com.elseeker.bible.domain.bible.result.BibleResult

object BibleViewResponse {

    data class Translation(
        val translationId: Long,
        val translationType: BibleTranslationType,
        val translationName: String,
        val translationLanguage: LanguageCode
    ) {
        companion object {
            fun from(result: BibleResult.Translation) =
                Translation(
                    translationId = result.translationId,
                    translationType = result.translationType,
                    translationName = result.translationName,
                    translationLanguage = result.translationLanguage
                )
        }
    }
}