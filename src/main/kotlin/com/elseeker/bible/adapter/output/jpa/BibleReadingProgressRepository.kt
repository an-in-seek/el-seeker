package com.elseeker.bible.adapter.output.jpa

import com.elseeker.bible.domain.model.BibleReadingProgress
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BibleReadingProgressRepository : JpaRepository<BibleReadingProgress, Long> {

    fun findAllByMemberAndTranslationIdAndBookOrder(
        member: Member,
        translationId: Long,
        bookOrder: Int
    ): List<BibleReadingProgress>

    fun existsByMemberAndTranslationIdAndBookOrderAndChapterNumber(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): Boolean

    fun deleteAllByMember(member: Member)
}
