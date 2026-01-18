package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleTypingVerseProgress
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository

interface BibleTypingVerseProgressRepository : JpaRepository<BibleTypingVerseProgress, Long> {
    fun existsByMemberAndSessionKeyAndVerseNumber(member: Member, sessionKey: String, verseNumber: Int): Boolean
    fun findFirstByMemberAndSessionKeyAndVerseNumber(member: Member, sessionKey: String, verseNumber: Int): BibleTypingVerseProgress?
}
