package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleTypingVerseProgress
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository

interface BibleTypingVerseProgressRepository : JpaRepository<BibleTypingVerseProgress, Long> {
    fun findFirstByMemberAndSessionKeyAndVerseNumber(member: Member, sessionKey: String, verseNumber: Int): BibleTypingVerseProgress?
    fun findTopByMemberOrderByCreatedAtDesc(member: Member): BibleTypingVerseProgress?
    fun findTopByMemberAndTranslationIdAndBookOrderAndChapterNumberOrderByCreatedAtDesc(
        member: Member,
        translationId: Long,
        bookOrder: Int,
        chapterNumber: Int
    ): BibleTypingVerseProgress?
    fun findAllByMemberAndSessionKeyOrderByVerseNumberAsc(member: Member, sessionKey: String): List<BibleTypingVerseProgress>
}
