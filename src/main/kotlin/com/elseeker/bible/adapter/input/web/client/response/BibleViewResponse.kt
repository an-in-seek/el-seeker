package com.elseeker.bible.adapter.input.web.client.response

import com.elseeker.bible.domain.result.BibleResult
import com.elseeker.bible.domain.vo.BibleTranslationType
import com.neovisionaries.i18n.LanguageCode

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