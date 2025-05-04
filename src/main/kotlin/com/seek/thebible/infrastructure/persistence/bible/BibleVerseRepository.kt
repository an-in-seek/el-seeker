package com.seek.thebible.infrastructure.persistence.bible

import com.seek.thebible.domain.bible.model.BibleVerse
import com.seek.thebible.presentation.api.response.BibleSearchResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BibleVerseRepository : JpaRepository<BibleVerse, Long> {

    @Query(
        """
        SELECT new com.seek.thebible.presentation.api.response.BibleSearchResponse(
                    b.id,
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
        """
    )
    fun searchByTranslationAndText(
        @Param("translationId") translationId: Long,
        @Param("keyword") keyword: String
    ): List<BibleSearchResponse>

}
