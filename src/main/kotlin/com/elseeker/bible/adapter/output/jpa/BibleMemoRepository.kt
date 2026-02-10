package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleVerseMemo
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BibleMemoRepository : JpaRepository<BibleVerseMemo, Long> {

    fun deleteAllByMember(member: Member)

    fun findAllByMemberAndTranslationIdAndBookOrderAndChapterNumber(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): List<BibleVerseMemo>

    fun findAllByMemberUidAndTranslationIdAndBookOrderAndChapterNumber(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): List<BibleVerseMemo>

    fun findByMemberAndTranslationIdAndBookOrderAndChapterNumberAndVerseNumber(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int
    ): BibleVerseMemo?

    fun findByMemberUidAndTranslationIdAndBookOrderAndChapterNumberAndVerseNumber(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int,
        verseNumber: Int
    ): BibleVerseMemo?
}
