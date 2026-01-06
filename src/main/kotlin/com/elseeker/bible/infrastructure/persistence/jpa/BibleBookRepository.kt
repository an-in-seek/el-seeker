package com.elseeker.bible.infrastructure.persistence.jpa

import com.elseeker.bible.domain.bible.model.BibleBook
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BibleBookRepository : JpaRepository<BibleBook, Long> {

    @Query(
        """
            SELECT book
            FROM BibleBook book
            LEFT JOIN FETCH book.chapters
            WHERE book.translationId = :translationId
        """
    )
    fun findByTranslationId(translationId: Long): List<BibleBook>

    @Query(
        """
            SELECT book
            FROM BibleBook book
            LEFT JOIN FETCH book.chapters
            WHERE book.translationId = :translationId
            AND book.bookOrder = :bookOrder
        """
    )
    fun findByTranslationAndBook(
        translationId: Long,
        bookOrder: Int
    ): BibleBook?
}
