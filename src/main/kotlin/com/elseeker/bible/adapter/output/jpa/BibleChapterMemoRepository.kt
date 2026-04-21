package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleChapterMemo
import com.elseeker.member.domain.model.Member
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BibleChapterMemoRepository : JpaRepository<BibleChapterMemo, Long> {

    fun findByMemberUidAndTranslationIdAndBookOrderAndChapterNumber(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): BibleChapterMemo?

    fun findByMemberAndTranslationIdAndBookOrderAndChapterNumber(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): BibleChapterMemo?

    fun deleteAllByMember(member: Member)

    fun countByMemberUid(memberUid: UUID): Long

    fun countByMemberUidAndTranslationId(memberUid: UUID, translationId: Long): Long

    fun countByMemberUidAndTranslationIdAndBookOrder(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int
    ): Long

    fun findAllByMemberUid(memberUid: UUID, pageable: Pageable): Slice<BibleChapterMemo>

    fun findAllByMemberUidAndTranslationId(
        memberUid: UUID,
        translationId: Long,
        pageable: Pageable
    ): Slice<BibleChapterMemo>

    fun findAllByMemberUidAndTranslationIdAndBookOrder(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int,
        pageable: Pageable
    ): Slice<BibleChapterMemo>

    @Query("SELECT DISTINCT m.translationId FROM BibleChapterMemo m WHERE m.member.uid = :memberUid ORDER BY m.translationId")
    fun findDistinctTranslationIdsByMemberUid(memberUid: UUID): List<Long>

    @Query("SELECT DISTINCT m.bookOrder FROM BibleChapterMemo m WHERE m.member.uid = :memberUid AND m.translationId = :translationId ORDER BY m.bookOrder")
    fun findDistinctBookOrdersByMemberUidAndTranslationId(memberUid: UUID, translationId: Long): List<Int>
}
