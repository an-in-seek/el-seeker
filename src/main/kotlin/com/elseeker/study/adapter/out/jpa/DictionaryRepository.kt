package com.elseeker.study.adapter.out.jpa

import com.elseeker.study.domain.model.Dictionary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DictionaryRepository : JpaRepository<Dictionary, Long> {

    fun findByTermContainingIgnoreCase(term: String, pageable: Pageable): Page<Dictionary>
}