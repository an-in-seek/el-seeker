package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.game.domain.vo.GameType
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "game_ranking",
    uniqueConstraints = [
        UniqueConstraint(name = "UK_game_ranking_member_type", columnNames = ["member_id", "game_type"])
    ],
    indexes = [
        Index(name = "IDX_game_ranking_type_rank", columnList = "game_type, ranking_position"),
        Index(name = "IDX_game_ranking_type_score", columnList = "game_type, ranking_score DESC")
    ]
)
class GameRanking(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false, length = 20)
    val gameType: GameType,

    @Column(name = "ranking_score", nullable = false, precision = 7, scale = 2)
    var rankingScore: BigDecimal = BigDecimal.ZERO,

    @Column(name = "completed_count", nullable = false)
    var completedCount: Int = 0,

    @Column(name = "perfect_count", nullable = false)
    var perfectCount: Int = 0,

    @Column(name = "ranking_position", nullable = false)
    var rankingPosition: Int = 0,

    @Column(name = "calculated_at", nullable = false)
    var calculatedAt: Instant = Instant.now()

) : BaseTimeEntity(id = id) {

    fun updateScore(rankingScore: BigDecimal, completedCount: Int, perfectCount: Int) {
        this.rankingScore = rankingScore
        this.completedCount = completedCount
        this.perfectCount = perfectCount
        this.calculatedAt = Instant.now()
    }
}
