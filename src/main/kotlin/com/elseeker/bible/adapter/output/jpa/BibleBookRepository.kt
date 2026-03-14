package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleBook
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
            ORDER BY book.bookOrder ASC
        """
    )
    fun findByTranslationId(translationId: Long): List<BibleBook>

    fun findByTranslationId(translationId: Long, pageable: Pageable): Page<BibleBook>

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

    @Query(
        """
            SELECT book
            FROM BibleBook book
            WHERE book.translationId = :translationId
            AND book.bookOrder IN :bookOrders
        """
    )
    fun findByTranslationIdAndBookOrderIn(
        translationId: Long,
        bookOrders: List<Int>
    ): List<BibleBook>
}
