package com.elseeker.bible.infrastructure.persistence.jpa

import com.elseeker.bible.domain.bible.model.BibleTranslation
import com.elseeker.bible.domain.bible.model.BibleTranslationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BibleTranslationRepository : JpaRepository<BibleTranslation, Long> {

    fun findAllByTranslationTypeInOrderByTranslationOrder(translationTypes: Set<BibleTranslationType>): List<BibleTranslation>

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
