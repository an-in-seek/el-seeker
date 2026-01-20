package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleTypingSession
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface BibleTypingSessionRepository : JpaRepository<BibleTypingSession, Long> {

    fun deleteAllByMember(member: Member)

    @Query(
        """
        SELECT session
        FROM BibleTypingSession session
        WHERE session.member = :member
          AND (:translationId IS NULL OR session.translationId = :translationId)
          AND (:bookOrder IS NULL OR session.bookOrder = :bookOrder)
          AND (:chapterNumber IS NULL OR session.chapterNumber = :chapterNumber)
          AND session.createdAt >= :fromDate
          AND session.createdAt <= :toDate
        ORDER BY session.createdAt DESC
        """
    )
    fun findSessions(
        @Param("member") member: Member,
        @Param("translationId") translationId: Long?,
        @Param("bookOrder") bookOrder: Int?,
        @Param("chapterNumber") chapterNumber: Int?,
        @Param("fromDate") fromDate: Instant,
        @Param("toDate") toDate: Instant
    ): List<BibleTypingSession>
}
