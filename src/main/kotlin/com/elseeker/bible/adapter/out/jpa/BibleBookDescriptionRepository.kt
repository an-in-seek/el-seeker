package com.elseeker.bible.adapter.out.jpa

import com.elseeker.bible.domain.model.BibleBookDescription
import com.elseeker.bible.domain.model.BibleBookDescriptionId
import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.bible.domain.vo.LanguageCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BibleBookDescriptionRepository : JpaRepository<BibleBookDescription, BibleBookDescriptionId> {
    fun findByBookKeyAndLanguageCode(bookKey: BibleBookKey, languageCode: LanguageCode): BibleBookDescription?
}
