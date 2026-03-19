package com.elseeker.study.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.study.adapter.output.jpa.DictionaryReferenceRepository
import com.elseeker.study.adapter.output.jpa.DictionaryRepository
import com.elseeker.study.domain.model.DictionaryReference
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminDictionaryReferenceService(
    private val dictionaryRepository: DictionaryRepository,
    private val dictionaryReferenceRepository: DictionaryReferenceRepository
) {

    fun findAllByDictionaryId(dictionaryId: Long): List<DictionaryReference> =
        dictionaryReferenceRepository.findAllByDictionaryIdOrderByDisplayOrderAsc(dictionaryId)

    @Transactional
    fun create(
        dictionaryId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int,
        verseLabel: String,
        displayOrder: Int
    ): DictionaryReference {
        val dictionary = dictionaryRepository.findByIdOrNull(dictionaryId)
            ?: throwError(ErrorType.DICTIONARY_NOT_FOUND, "id=$dictionaryId")

        if (dictionaryReferenceRepository.existsByDictionaryIdAndBookOrderAndChapterNumberAndVerseNumber(
                dictionaryId, bookOrder, chapterNumber, verseNumber
            )
        ) {
            throwError(ErrorType.INVALID_PARAMETER, "이미 등록된 구절입니다: $verseLabel")
        }

        return dictionaryReferenceRepository.save(
            DictionaryReference(
                dictionary = dictionary,
                bookOrder = bookOrder,
                chapterNumber = chapterNumber,
                verseNumber = verseNumber,
                verseLabel = verseLabel,
                displayOrder = displayOrder
            )
        )
    }

    @Transactional
    fun update(
        dictionaryId: Long,
        referenceId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int,
        verseLabel: String,
        displayOrder: Int
    ): DictionaryReference {
        val reference = findReference(dictionaryId, referenceId)

        val verseChanged = reference.bookOrder != bookOrder
                || reference.chapterNumber != chapterNumber
                || reference.verseNumber != verseNumber
        if (verseChanged && dictionaryReferenceRepository.existsByDictionaryIdAndBookOrderAndChapterNumberAndVerseNumber(
                dictionaryId, bookOrder, chapterNumber, verseNumber
            )
        ) {
            throwError(ErrorType.INVALID_PARAMETER, "이미 등록된 구절입니다: $verseLabel")
        }

        val updated = DictionaryReference(
            id = reference.id,
            dictionary = reference.dictionary,
            bookOrder = bookOrder,
            chapterNumber = chapterNumber,
            verseNumber = verseNumber,
            verseLabel = verseLabel,
            displayOrder = displayOrder,
            createdAt = reference.createdAt
        )
        return dictionaryReferenceRepository.save(updated)
    }

    @Transactional
    fun delete(dictionaryId: Long, referenceId: Long) {
        val reference = findReference(dictionaryId, referenceId)
        dictionaryReferenceRepository.delete(reference)
    }

    @Transactional
    fun updateOrder(dictionaryId: Long, referenceIds: List<Long>) {
        val references = dictionaryReferenceRepository.findAllByDictionaryIdOrderByDisplayOrderAsc(dictionaryId)
        val refMap = references.associateBy { it.id!! }

        referenceIds.forEachIndexed { index, refId ->
            val ref = refMap[refId] ?: return@forEachIndexed
            ref.displayOrder = index
        }
    }

    private fun findReference(dictionaryId: Long, referenceId: Long): DictionaryReference {
        val reference = dictionaryReferenceRepository.findByIdOrNull(referenceId)
            ?: throwError(ErrorType.INVALID_PARAMETER, "관련 구절을 찾을 수 없습니다: id=$referenceId")

        if (reference.dictionary.id != dictionaryId) {
            throwError(ErrorType.INVALID_PARAMETER, "해당 사전 항목의 관련 구절이 아닙니다")
        }
        return reference
    }
}
