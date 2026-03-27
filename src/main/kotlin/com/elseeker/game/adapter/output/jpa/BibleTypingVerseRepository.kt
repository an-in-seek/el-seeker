package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.BibleTypingVerse
import com.elseeker.game.domain.model.BibleTypingVerseId
import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface BibleTypingVerseRepository : JpaRepository<BibleTypingVerse, BibleTypingVerseId> {

    fun findAllBySessionSessionKey(sessionKey: UUID): List<BibleTypingVerse>

    @Query(
        """
        SELECT AVG(v.accuracy) AS avgAccuracy,
               AVG(v.cpm) AS avgCpm,
               COUNT(v) AS completedCount,
               SUM(CASE WHEN v.accuracy = 100.0 THEN 1 ELSE 0 END) AS perfectCount
        FROM BibleTypingVerse v
        JOIN v.session s
        WHERE s.member = :member
        AND v.completed = true
        """
    )
    fun findTypingStatsByMember(@Param("member") member: Member): TypingStatsRow?
}

interface TypingStatsRow {
    val avgAccuracy: Double?
    val avgCpm: Double?
    val completedCount: Long
    val perfectCount: Long
}
