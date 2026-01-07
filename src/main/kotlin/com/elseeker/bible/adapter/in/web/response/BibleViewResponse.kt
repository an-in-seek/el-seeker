package com.elseeker.bible.adapter.`in`.web.response

import com.elseeker.bible.domain.result.BibleResult
import com.elseeker.bible.domain.vo.BibleTranslationType
import com.elseeker.bible.domain.vo.LanguageCode

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