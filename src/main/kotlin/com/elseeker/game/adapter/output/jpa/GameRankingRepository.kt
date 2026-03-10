package com.elseeker.game.adapter.output.jpa

import com.elseeker.game.domain.model.GameRanking
import com.elseeker.game.domain.vo.GameType
import com.elseeker.member.domain.model.Member
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface GameRankingRepository : JpaRepository<GameRanking, Long> {

    @Query(
        """
        SELECT gr FROM GameRanking gr
        JOIN FETCH gr.member
        WHERE gr.gameType = :gameType
        ORDER BY gr.rankingPosition ASC
        """
    )
    fun findTopByGameType(gameType: GameType, pageable: Pageable): List<GameRanking>

    @Query(
        """
        SELECT gr FROM GameRanking gr
        JOIN FETCH gr.member
        WHERE gr.member = :member AND gr.gameType = :gameType
        """
    )
    fun findByMemberAndGameType(member: Member, gameType: GameType): GameRanking?

    @Query(
        """
        SELECT COUNT(gr) FROM GameRanking gr
        WHERE gr.gameType = :gameType
        """
    )
    fun countByGameType(gameType: GameType): Long

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        UPDATE GameRanking gr
        SET gr.rankingPosition = (
            SELECT COUNT(gr2) + 1 FROM GameRanking gr2
            WHERE gr2.gameType = gr.gameType
            AND (
                gr2.rankingScore > gr.rankingScore
                OR (gr2.rankingScore = gr.rankingScore AND gr2.perfectCount > gr.perfectCount)
                OR (gr2.rankingScore = gr.rankingScore AND gr2.perfectCount = gr.perfectCount AND gr2.completedCount > gr.completedCount)
                OR (gr2.rankingScore = gr.rankingScore AND gr2.perfectCount = gr.perfectCount AND gr2.completedCount = gr.completedCount AND gr2.calculatedAt < gr.calculatedAt)
            )
        )
        WHERE gr.gameType = :gameType
        """
    )
    fun recalculateRankPositions(gameType: GameType)

    @Modifying
    @Query("DELETE FROM GameRanking gr WHERE gr.member.id = :memberId")
    fun deleteAllByMemberId(memberId: Long)
}
