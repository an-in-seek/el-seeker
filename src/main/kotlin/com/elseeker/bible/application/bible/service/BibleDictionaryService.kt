package com.elseeker.bible.application.bible.service

import com.elseeker.bible.domain.ErrorType
import com.elseeker.bible.domain.ServiceError
import com.elseeker.bible.domain.bible.model.BibleDictionary
import com.elseeker.bible.infrastructure.persistence.jpa.BibleDictionaryRepository
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
class BibleDictionaryService(
    private val bibleDictionaryRepository: BibleDictionaryRepository
) {

    fun getDictionaries(keyword: String?, pageable: Pageable): Page<BibleDictionary> =
        if (keyword.isNullOrBlank()) {
            bibleDictionaryRepository.findAll(pageable)
        } else {
            bibleDictionaryRepository.findByTermContainingIgnoreCase(keyword.trim(), pageable)
        }

    fun getDictionary(id: Long): BibleDictionary =
        bibleDictionaryRepository.findById(id)
            .orElseThrow { ServiceError(ErrorType.DICTIONARY_NOT_FOUND, "id=$id") }
}
