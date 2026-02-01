package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleVerseHighlight
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository

interface BibleHighlightRepository : JpaRepository<BibleVerseHighlight, Long> {

    fun findAllByMemberAndTranslationIdAndBookOrderAndChapterNumber(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): List<BibleVerseHighlight>

    fun findByMemberAndTranslationIdAndBookOrderAndChapterNumberAndVerseNumber(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int
    ): BibleVerseHighlight?

    fun deleteAllByMember(member: Member)
}
