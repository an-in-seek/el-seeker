package com.seek.thebible.presentation.bible.dto

import com.seek.thebible.domain.bible.dto.TranslationResult
import com.seek.thebible.domain.bible.model.BibleTranslationType

data class BibleTranslationResponse(
    val translationId: Long,
    val translationType: BibleTranslationType,
    val translationName: String
) {
    companion object {
        fun from(result: TranslationResult) =
            BibleTranslationResponse(
                translationId = result.translationId,
                translationType = result.translationType,
                translationName = result.translationName
            )
    }
}