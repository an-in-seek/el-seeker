package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleVerseHighlight
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BibleHighlightRepository : JpaRepository<BibleVerseHighlight, Long> {

    fun findAllByMemberAndTranslationIdAndBookOrderAndChapterNumber(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): List<BibleVerseHighlight>

    fun findAllByMemberUidAndTranslationIdAndBookOrderAndChapterNumber(
        memberUid: UUID,
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

    fun findByMemberUidAndTranslationIdAndBookOrderAndChapterNumberAndVerseNumber(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int
    ): BibleVerseHighlight?

    fun deleteAllByMember(member: Member)
}
