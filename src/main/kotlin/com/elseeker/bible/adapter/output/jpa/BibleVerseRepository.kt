package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.adapter.input.api.response.BibleSearchResponse
import com.elseeker.bible.domain.model.BibleVerse
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
        SELECT new com.elseeker.bible.adapter.input.api.response.BibleSearchResponse(
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
        WHERE b.translationId = :translationId AND LOWER(v.text) LIKE LOWER(CONCAT('%', :keyword, '%'))
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
        WHERE b.translationId = :translationId AND LOWER(v.text) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """
    )
    fun countByTranslationAndText(
        @Param("translationId") translationId: Long,
        @Param("keyword") keyword: String
    ): Long

}
