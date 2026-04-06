package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleBookMemo
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
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
}
