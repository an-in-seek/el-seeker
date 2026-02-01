package com.elseeker.bible.application.service

import com.elseeker.bible.adapter.output.jpa.BibleTranslationRepository
import com.elseeker.bible.domain.model.BibleTranslation
import com.elseeker.bible.domain.vo.BibleTranslationType
import com.elseeker.bible.domain.vo.LanguageCode
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminBibleTranslationService(
    private val translationRepository: BibleTranslationRepository,
) {
    fun findAll(pageable: Pageable): Page<BibleTranslation> =
        translationRepository.findAll(pageable)

    fun findById(id: Long): BibleTranslation =
        translationRepository.findByIdOrNull(id) ?: throwError(ErrorType.TRANSLATION_NOT_FOUND, "id=$id")

    @Transactional
    fun create(translationType: BibleTranslationType, name: String, translationOrder: Int, languageCode: LanguageCode): BibleTranslation =
        translationRepository.save(
            BibleTranslation(
                translationType = translationType,
                name = name,
                translationOrder = translationOrder,
                languageCode = languageCode,
            )
        )

    @Transactional
    fun update(id: Long, translationType: BibleTranslationType, name: String, translationOrder: Int, languageCode: LanguageCode): BibleTranslation {
        val existing = findById(id)
        val updated = BibleTranslation(
            id = existing.id,
            translationType = translationType,
            name = name,
            translationOrder = translationOrder,
            languageCode = languageCode,
        )
        return translationRepository.save(updated)
    }

    @Transactional
    fun delete(id: Long) {
        val entity = findById(id)
        translationRepository.delete(entity)
    }
}
