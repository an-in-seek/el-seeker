package com.elseeker.bible.infrastructure.persistence.jpa.bible

import com.elseeker.bible.domain.bible.model.BibleChapter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BibleChapterRepository : JpaRepository<BibleChapter, Long> {

    @Query(
        """
            SELECT MAX(c.chapterNumber)
            FROM BibleChapter c
            WHERE c.bookId = :bookId
        """
    )
    fun findMaxChapterNumberByBookId(bookId: Long): Int?

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
