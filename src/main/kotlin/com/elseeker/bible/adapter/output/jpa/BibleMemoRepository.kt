package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleVerseMemo
import com.elseeker.member.domain.model.Member
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BibleMemoRepository : JpaRepository<BibleVerseMemo, Long> {

    fun deleteAllByMember(member: Member)

    fun countByMemberUid(memberUid: UUID): Long

    fun findAllByMemberUid(memberUid: UUID, pageable: Pageable): Slice<BibleVerseMemo>

    fun findAllByMemberUidAndBookOrder(memberUid: UUID, bookOrder: Int, pageable: Pageable): Slice<BibleVerseMemo>

    fun countByMemberUidAndBookOrder(memberUid: UUID, bookOrder: Int): Long

    @Query("SELECT DISTINCT m.translationId FROM BibleVerseMemo m WHERE m.member.uid = :memberUid ORDER BY m.translationId")
    fun findDistinctTranslationIdsByMemberUid(memberUid: UUID): List<Long>

    @Query("SELECT DISTINCT m.bookOrder FROM BibleVerseMemo m WHERE m.member.uid = :memberUid ORDER BY m.bookOrder")
    fun findDistinctBookOrdersByMemberUid(memberUid: UUID): List<Int>

    @Query("SELECT DISTINCT m.bookOrder FROM BibleVerseMemo m WHERE m.member.uid = :memberUid AND m.translationId = :translationId ORDER BY m.bookOrder")
    fun findDistinctBookOrdersByMemberUidAndTranslationId(memberUid: UUID, translationId: Long): List<Int>

    fun findAllByMemberUidAndTranslationId(memberUid: UUID, translationId: Long, pageable: Pageable): Slice<BibleVerseMemo>

    fun findAllByMemberUidAndTranslationIdAndBookOrder(memberUid: UUID, translationId: Long, bookOrder: Int, pageable: Pageable): Slice<BibleVerseMemo>

    fun countByMemberUidAndTranslationId(memberUid: UUID, translationId: Long): Long

    fun countByMemberUidAndTranslationIdAndBookOrder(memberUid: UUID, translationId: Long, bookOrder: Int): Long

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
