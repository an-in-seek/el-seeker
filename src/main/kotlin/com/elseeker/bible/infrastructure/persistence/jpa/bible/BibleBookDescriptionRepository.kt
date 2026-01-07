package com.elseeker.bible.infrastructure.persistence.jpa.bible

import com.elseeker.bible.domain.bible.model.BibleBookDescription
import com.elseeker.bible.domain.bible.model.BibleBookDescriptionId
import com.elseeker.bible.domain.bible.vo.BibleBookKey
import com.elseeker.bible.domain.bible.vo.LanguageCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BibleBookDescriptionRepository : JpaRepository<BibleBookDescription, BibleBookDescriptionId> {
    fun findByBookKeyAndLanguageCode(bookKey: BibleBookKey, languageCode: LanguageCode): BibleBookDescription?
}
