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

/**
 * 성경 사전 서비스
 */
@Service
@Transactional(readOnly = true)
class DictionaryService(
    private val dictionaryRepository: DictionaryRepository
) {

    fun getDictionaries(keyword: String?, pageable: Pageable): Page<Dictionary> =
        if (keyword.isNullOrBlank()) {
            dictionaryRepository.findAllOrderByKo(pageable)
        } else {
            dictionaryRepository.findByTermContainingKo(keyword.trim(), pageable)
        }

    fun getDictionary(id: Long): Dictionary =
        dictionaryRepository.findByIdOrNull(id) ?: throwError(ErrorType.DICTIONARY_NOT_FOUND, "id=$id")
}
