package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleTypingSession
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface BibleTypingSessionRepository : JpaRepository<BibleTypingSession, Long> {

    fun deleteAllByMember(member: Member)

    fun findBySessionKeyAndMember(sessionUid: UUID, member: Member): BibleTypingSession?

    @Query(
        """
        SELECT session
        FROM BibleTypingSession session
        LEFT JOIN FETCH session.verses verse
        WHERE session.member = :member
          AND (:translationId IS NULL OR session.translationId = :translationId)
          AND (:bookOrder IS NULL OR session.bookOrder = :bookOrder)
          AND (:chapterNumber IS NULL OR session.chapterNumber = :chapterNumber)
        """
    )
    fun findSession(
        @Param("member") member: Member,
        @Param("translationId") translationId: Long?,
        @Param("bookOrder") bookOrder: Int?,
        @Param("chapterNumber") chapterNumber: Int?,
    ): BibleTypingSession?

    // 최신 세션 조회
    fun findTopByMemberOrderByCreatedAtDesc(
        member: Member
    ): BibleTypingSession?

}
