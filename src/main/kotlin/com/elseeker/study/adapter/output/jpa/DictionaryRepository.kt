package com.elseeker.study.adapter.output.jpa

import com.elseeker.study.domain.model.Dictionary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DictionaryRepository : JpaRepository<Dictionary, Long> {

    @Query(
        value = """
        SELECT * 
        FROM dictionary d
        ORDER BY d.term COLLATE "ko_KR.utf8"
        """,
        countQuery = "SELECT count(*) FROM dictionary",
        nativeQuery = true
    )
    fun findAllOrderByKo(pageable: Pageable): Page<Dictionary>

    @Query(
        value = """
        SELECT * 
        FROM dictionary d
        WHERE d.term ILIKE CONCAT('%', :term, '%')
        ORDER BY d.term COLLATE "ko_KR.utf8"
        """,
        countQuery = """
        SELECT count(*) 
        FROM dictionary d
        WHERE d.term ILIKE CONCAT('%', :term, '%')
        """,
        nativeQuery = true
    )
    fun findByTermContainingKo(@Param("term") term: String, pageable: Pageable): Page<Dictionary>

    @Query(
        """
        SELECT d FROM Dictionary d
        LEFT JOIN FETCH d.references
        WHERE d.id IN :ids
        ORDER BY d.term ASC
        """
    )
    fun findAllByIdWithReferences(@Param("ids") ids: List<Long>): List<Dictionary>
}
