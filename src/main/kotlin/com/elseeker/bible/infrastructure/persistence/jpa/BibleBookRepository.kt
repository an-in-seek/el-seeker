package com.elseeker.bible.infrastructure.persistence.jpa

import com.elseeker.bible.domain.bible.model.BibleBook
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BibleBookRepository : JpaRepository<BibleBook, Long> {

    fun findByTranslationId(translationId: Long): List<BibleBook>

    @Query(
        """
            SELECT b
            FROM BibleBook b
            LEFT JOIN FETCH b.chapters
            WHERE 1=1 
            AND b.translationId = :translationId
            AND b.bookOrder = :bookOrder
        """
    )
    fun findByTranslationAndBook(
        translationId: Long,
        bookOrder: Int
    ): BibleBook?
}
