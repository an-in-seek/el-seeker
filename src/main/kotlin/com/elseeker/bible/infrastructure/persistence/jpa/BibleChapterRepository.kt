package com.elseeker.bible.infrastructure.persistence.jpa

import com.elseeker.bible.domain.bible.model.BibleChapter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BibleChapterRepository : JpaRepository<BibleChapter, Long> {

    fun countByBookId(bookId: Long): Int

    @Query(
        """
            SELECT c 
            FROM BibleChapter c
            LEFT JOIN FETCH c.verses
            WHERE 1=1 
            AND c.bookId = :bookId
            AND c.chapterNumber = :chapterNumber
        """
    )
    fun findByBookAndChapter(bookId: Long, chapterNumber: Int): BibleChapter?
}
