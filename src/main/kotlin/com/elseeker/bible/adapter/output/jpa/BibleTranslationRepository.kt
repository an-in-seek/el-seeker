package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleTranslation
import com.elseeker.bible.domain.vo.BibleTranslationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BibleTranslationRepository : JpaRepository<BibleTranslation, Long> {

    fun findAllByTranslationTypeInOrderByTranslationOrder(translationTypes: Set<BibleTranslationType>): List<BibleTranslation>

    fun findByTranslationType(translationType: BibleTranslationType): BibleTranslation?

    @Query(
        """
        SELECT t 
        FROM BibleTranslation t 
        INNER JOIN FETCH t.books 
        WHERE t.id = :id
        """
    )
    fun findByIdWithBooks(id: Long): BibleTranslation?
}
