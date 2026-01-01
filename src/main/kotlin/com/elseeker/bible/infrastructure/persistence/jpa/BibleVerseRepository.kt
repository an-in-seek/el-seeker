package com.elseeker.bible.infrastructure.persistence.jpa

import com.elseeker.bible.domain.bible.model.BibleVerse
import com.elseeker.bible.presentation.api.response.BibleSearchResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BibleVerseRepository : JpaRepository<BibleVerse, Long> {

    @Query(
        """
        SELECT new com.elseeker.bible.presentation.api.response.BibleSearchResponse(
                    b.id,
                    b.bookOrder,
                    b.name,
                    c.id,
                    c.chapterNumber,
                    v.id,
                    v.verseNumber, 
                    v.text
                ) 
        FROM BibleVerse v
        JOIN BibleChapter c ON v.chapterId = c.id
        JOIN BibleBook b ON c.bookId = b.id
        JOIN BibleTranslation t ON b.translationId = t.id
        WHERE t.id = :translationId AND LOWER(v.text) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY b.bookOrder, c.chapterNumber, v.verseNumber
        """
    )
    fun searchSliceByTranslationAndText(
        @Param("translationId") translationId: Long,
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Slice<BibleSearchResponse>

    @Query(
        """
        SELECT COUNT(v.id)
        FROM BibleVerse v
        JOIN BibleChapter c ON v.chapterId = c.id
        JOIN BibleBook b ON c.bookId = b.id
        JOIN BibleTranslation t ON b.translationId = t.id
        WHERE t.id = :translationId AND LOWER(v.text) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """
    )
    fun countByTranslationAndText(
        @Param("translationId") translationId: Long,
        @Param("keyword") keyword: String
    ): Long

}
