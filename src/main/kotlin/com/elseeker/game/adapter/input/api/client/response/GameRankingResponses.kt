package com.elseeker.game.adapter.input.api.client.response

import com.elseeker.game.domain.model.GameRanking
import com.elseeker.game.domain.vo.GameType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.math.RoundingMode

@Schema(description = "게임 랭킹 목록 응답")
data class GameRankingListResponse(
    @field:Schema(description = "게임 유형", example = "OX_QUIZ")
    val gameType: GameType,

    @field:Schema(description = "총 참여자 수", example = "234")
    val totalParticipants: Long,

    @field:Schema(description = "랭킹 목록")
    val rankings: List<GameRankingItemResponse>,

    @field:Schema(description = "내 순위 (비로그인 시 null)")
    val myRanking: MyRankingResponse?
)

@Schema(description = "랭킹 항목")
data class GameRankingItemResponse(
    @field:Schema(description = "순위", example = "1")
    val rank: Int,

    @field:Schema(description = "닉네임", example = "성경마스터")
    val nickname: String,

    @field:Schema(description = "프로필 이미지 URL")
    val profileImageUrl: String?,

    @field:Schema(description = "랭킹 점수", example = "580.00")
    val rankingScore: BigDecimal,

    @field:Schema(description = "완료 수", example = "62")
    val completedCount: Int,

    @field:Schema(description = "만점 수", example = "45")
    val perfectCount: Int
) {
    companion object {
        fun from(ranking: GameRanking): GameRankingItemResponse {
            val member = ranking.member
            return GameRankingItemResponse(
                rank = ranking.rankingPosition,
                nickname = member.nickname,
                profileImageUrl = member.profileImageUrl,
                rankingScore = ranking.rankingScore,
                completedCount = ranking.completedCount,
                perfectCount = ranking.perfectCount
            )
        }
    }
}

@Schema(description = "내 순위 응답")
data class MyRankingResponse(
    @field:Schema(description = "순위", example = "15")
    val rank: Int,

    @field:Schema(description = "랭킹 점수", example = "320.00")
    val rankingScore: BigDecimal,

    @field:Schema(description = "완료 수", example = "40")
    val completedCount: Int,

    @field:Schema(description = "만점 수", example = "12")
    val perfectCount: Int,

    @field:Schema(description = "상위 퍼센트", example = "6.4")
    val topPercent: BigDecimal?
) {
    companion object {
        fun from(ranking: GameRanking, totalParticipants: Long): MyRankingResponse {
            val topPercent = if (totalParticipants > 0) {
                BigDecimal.valueOf(ranking.rankingPosition * 100.0 / totalParticipants)
                    .setScale(1, RoundingMode.HALF_UP)
            } else null

            return MyRankingResponse(
                rank = ranking.rankingPosition,
                rankingScore = ranking.rankingScore,
                completedCount = ranking.completedCount,
                perfectCount = ranking.perfectCount,
                topPercent = topPercent
            )
        }
    }
}

@Schema(description = "내 순위 상세 응답")
data class MyRankingDetailResponse(
    @field:Schema(description = "게임 유형", example = "OX_QUIZ")
    val gameType: GameType,

    @field:Schema(description = "순위", example = "15")
    val rank: Int,

    @field:Schema(description = "총 참여자 수", example = "234")
    val totalParticipants: Long,

    @field:Schema(description = "랭킹 점수", example = "320.00")
    val rankingScore: BigDecimal,

    @field:Schema(description = "완료 수", example = "40")
    val completedCount: Int,

    @field:Schema(description = "만점 수", example = "12")
    val perfectCount: Int,

    @field:Schema(description = "상위 퍼센트", example = "6.4")
    val topPercent: BigDecimal?
)
