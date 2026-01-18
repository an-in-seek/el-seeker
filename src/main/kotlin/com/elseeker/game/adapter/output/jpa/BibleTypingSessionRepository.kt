package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleTypingSession
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface BibleTypingSessionRepository : JpaRepository<BibleTypingSession, Long> {

    @Query(
        """
        SELECT session
        FROM BibleTypingSession session
        WHERE session.member = :member
          AND (:translationId IS NULL OR session.translationId = :translationId)
          AND (:bookOrder IS NULL OR session.bookOrder = :bookOrder)
          AND (:chapterNumber IS NULL OR session.chapterNumber = :chapterNumber)
          AND (:fromDate IS NULL OR session.createdAt >= :fromDate)
          AND (:toDate IS NULL OR session.createdAt <= :toDate)
        ORDER BY session.createdAt DESC
        """
    )
    fun findSessions(
        @Param("member") member: Member,
        @Param("translationId") translationId: Long?,
        @Param("bookOrder") bookOrder: Int?,
        @Param("chapterNumber") chapterNumber: Int?,
        @Param("fromDate") fromDate: LocalDateTime?,
        @Param("toDate") toDate: LocalDateTime?
    ): List<BibleTypingSession>
}
