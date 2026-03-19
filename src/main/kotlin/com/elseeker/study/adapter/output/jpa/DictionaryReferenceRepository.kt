package com.elseeker.study.adapter.output.jpa

import com.elseeker.study.domain.model.DictionaryReference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DictionaryReferenceRepository : JpaRepository<DictionaryReference, Long> {

    fun findAllByDictionaryIdOrderByDisplayOrderAsc(dictionaryId: Long): List<DictionaryReference>

    fun existsByDictionaryIdAndBookOrderAndChapterNumberAndVerseNumber(
        dictionaryId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int
    ): Boolean
}
