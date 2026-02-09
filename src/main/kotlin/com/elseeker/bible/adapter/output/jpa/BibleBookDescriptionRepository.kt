package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleBookDescription
import com.elseeker.bible.domain.vo.BibleBookKey
import com.neovisionaries.i18n.LanguageCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BibleBookDescriptionRepository : JpaRepository<BibleBookDescription, Long> {

    @Query("select b from BibleBookDescription b where b.bookKey = :bookKey and b.languageCode = :languageCode")
    fun findByBookKeyAndLanguageCode(
        @Param("bookKey") bookKey: BibleBookKey,
        @Param("languageCode") languageCode: LanguageCode
    ): BibleBookDescription?

    fun findByBookKey(bookKey: BibleBookKey, pageable: Pageable): Page<BibleBookDescription>

    fun findByLanguageCode(languageCode: LanguageCode, pageable: Pageable): Page<BibleBookDescription>

    fun findByBookKeyAndLanguageCode(bookKey: BibleBookKey, languageCode: LanguageCode, pageable: Pageable): Page<BibleBookDescription>
}
