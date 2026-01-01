package com.elseeker.bible.infrastructure.persistence.jpa

import com.elseeker.bible.domain.bible.model.BibleDictionary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BibleDictionaryRepository : JpaRepository<BibleDictionary, Long> {

    fun findByTermContainingIgnoreCase(term: String, pageable: Pageable): Page<BibleDictionary>
}
