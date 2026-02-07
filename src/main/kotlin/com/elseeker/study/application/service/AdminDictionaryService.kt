package com.elseeker.study.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.study.adapter.output.jpa.DictionaryRepository
import com.elseeker.study.domain.model.Dictionary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminDictionaryService(
    private val dictionaryRepository: DictionaryRepository,
) {
    fun findAll(keyword: String?, pageable: Pageable): Page<Dictionary> =
        if (keyword.isNullOrBlank()) {
            dictionaryRepository.findAllOrderByKo(pageable)
        } else {
            dictionaryRepository.findByTermContainingKo(keyword.trim(), pageable)
        }

    fun findById(id: Long): Dictionary =
        dictionaryRepository.findByIdOrNull(id) ?: throwError(ErrorType.DICTIONARY_NOT_FOUND, "id=$id")

    @Transactional
    fun create(term: String, description: String?, relatedVerses: String?): Dictionary =
        dictionaryRepository.save(Dictionary(term = term, description = description, relatedVerses = relatedVerses))

    @Transactional
    fun update(id: Long, term: String, description: String?, relatedVerses: String?): Dictionary {
        val existing = findById(id)
        val updated = Dictionary(
            id = existing.id,
            term = term,
            description = description,
            relatedVerses = relatedVerses,
        )
        return dictionaryRepository.save(updated)
    }

    @Transactional
    fun delete(id: Long) {
        val entity = findById(id)
        dictionaryRepository.delete(entity)
    }
}
