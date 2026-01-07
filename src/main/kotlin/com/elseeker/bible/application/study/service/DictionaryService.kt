package com.elseeker.bible.application.study.service

import com.elseeker.bible.domain.common.ErrorType
import com.elseeker.bible.domain.common.ServiceError
import com.elseeker.bible.domain.study.model.Dictionary
import com.elseeker.bible.infrastructure.persistence.jpa.study.DictionaryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 패키지 구조 안내:
 * - domain/bible/model: 엔티티
 * - infrastructure/persistence/jpa: Repository
 * - application/bible/service: 읽기 전용 비즈니스 서비스
 * - presentation/api, presentation/web: API/웹 컨트롤러와 응답 모델
 */
@Service
@Transactional(readOnly = true)
class DictionaryService(
    private val dictionaryRepository: DictionaryRepository
) {

    fun getDictionaries(keyword: String?, pageable: Pageable): Page<Dictionary> =
        if (keyword.isNullOrBlank()) {
            dictionaryRepository.findAll(pageable)
        } else {
            dictionaryRepository.findByTermContainingIgnoreCase(keyword.trim(), pageable)
        }

    fun getDictionary(id: Long): Dictionary =
        dictionaryRepository.findById(id)
            .orElseThrow { ServiceError(ErrorType.DICTIONARY_NOT_FOUND, "id=$id") }
}