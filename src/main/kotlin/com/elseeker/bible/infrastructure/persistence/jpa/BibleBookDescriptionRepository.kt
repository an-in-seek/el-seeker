package com.elseeker.bible.infrastructure.persistence.jpa

import com.elseeker.bible.domain.bible.model.BibleBookDescription
import com.elseeker.bible.domain.bible.model.BibleBookDescriptionId
import com.elseeker.bible.domain.bible.model.BibleBookKey
import com.elseeker.bible.domain.bible.model.LanguageCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BibleBookDescriptionRepository : JpaRepository<BibleBookDescription, BibleBookDescriptionId> {
    fun findByBookKeyAndLanguageCode(bookKey: BibleBookKey, languageCode: LanguageCode): BibleBookDescription?
}
