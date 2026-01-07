package com.elseeker.bible.infrastructure.persistence.jpa.study

import com.elseeker.bible.domain.study.model.Dictionary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DictionaryRepository : JpaRepository<Dictionary, Long> {

    fun findByTermContainingIgnoreCase(term: String, pageable: Pageable): Page<Dictionary>
}