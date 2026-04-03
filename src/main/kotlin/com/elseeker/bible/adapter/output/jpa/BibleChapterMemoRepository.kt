package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleChapterMemo
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
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
}
