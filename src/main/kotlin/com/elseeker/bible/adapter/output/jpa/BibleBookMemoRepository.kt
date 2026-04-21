package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleBookMemo
import com.elseeker.member.domain.model.Member
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BibleBookMemoRepository : JpaRepository<BibleBookMemo, Long> {

    fun findByMemberUidAndTranslationIdAndBookOrder(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int
    ): BibleBookMemo?

    fun findByMemberAndTranslationIdAndBookOrder(
        member: Member,
        translationId: Long,
        bookOrder: Int
    ): BibleBookMemo?

    fun deleteAllByMember(member: Member)

    fun countByMemberUid(memberUid: UUID): Long

    fun countByMemberUidAndTranslationId(memberUid: UUID, translationId: Long): Long

    fun countByMemberUidAndTranslationIdAndBookOrder(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int
    ): Long

    fun findAllByMemberUid(memberUid: UUID, pageable: Pageable): Slice<BibleBookMemo>

    fun findAllByMemberUidAndTranslationId(
        memberUid: UUID,
        translationId: Long,
        pageable: Pageable
    ): Slice<BibleBookMemo>

    fun findAllByMemberUidAndTranslationIdAndBookOrder(
        memberUid: UUID,
        translationId: Long,
        bookOrder: Int,
        pageable: Pageable
    ): Slice<BibleBookMemo>

    @Query("SELECT DISTINCT m.translationId FROM BibleBookMemo m WHERE m.member.uid = :memberUid ORDER BY m.translationId")
    fun findDistinctTranslationIdsByMemberUid(memberUid: UUID): List<Long>

    @Query("SELECT DISTINCT m.bookOrder FROM BibleBookMemo m WHERE m.member.uid = :memberUid AND m.translationId = :translationId ORDER BY m.bookOrder")
    fun findDistinctBookOrdersByMemberUidAndTranslationId(memberUid: UUID, translationId: Long): List<Int>
}
