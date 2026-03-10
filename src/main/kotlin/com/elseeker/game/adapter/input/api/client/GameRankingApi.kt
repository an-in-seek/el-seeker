package com.elseeker.game.adapter.input.api.client

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.client.response.*
import com.elseeker.game.application.service.GameRankingService
import com.elseeker.game.domain.vo.GameType
import com.elseeker.member.adapter.output.jpa.MemberRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode

@RestController
@RequestMapping("/api/v1/game/ranking")
class GameRankingApi(
    private val gameRankingService: GameRankingService,
    private val memberRepository: MemberRepository
) : GameRankingApiDocument {

    @GetMapping
    override fun getRankings(
        @RequestParam gameType: GameType,
        @RequestParam(defaultValue = "50") limit: Int,
        @AuthenticationPrincipal principal: JwtPrincipal?
    ): ResponseEntity<GameRankingListResponse> {
        val effectiveLimit = limit.coerceIn(1, 100)
        val rankings = gameRankingService.getRankings(gameType, effectiveLimit)
        val totalParticipants = gameRankingService.getTotalParticipants(gameType)

        val myRanking = principal?.let {
            val member = memberRepository.findByUid(it.memberUid) ?: return@let null
            val ranking = gameRankingService.getMyRanking(member, gameType) ?: return@let null
            MyRankingResponse.from(ranking, totalParticipants)
        }

        val response = GameRankingListResponse(
            gameType = gameType,
            totalParticipants = totalParticipants,
            rankings = rankings.map { GameRankingItemResponse.from(it) },
            myRanking = myRanking
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    override fun getMyRanking(
        @RequestParam gameType: GameType,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<MyRankingDetailResponse> {
        val member = memberRepository.findByUid(principal.memberUid)
            ?: return ResponseEntity.notFound().build()
        val ranking = gameRankingService.getMyRanking(member, gameType)
            ?: return ResponseEntity.notFound().build()
        val totalParticipants = gameRankingService.getTotalParticipants(gameType)

        val topPercent = if (totalParticipants > 0) {
            BigDecimal.valueOf(ranking.rankingPosition * 100.0 / totalParticipants)
                .setScale(1, RoundingMode.HALF_UP)
        } else null

        val response = MyRankingDetailResponse(
            gameType = gameType,
            rank = ranking.rankingPosition,
            totalParticipants = totalParticipants,
            rankingScore = ranking.rankingScore,
            completedCount = ranking.completedCount,
            perfectCount = ranking.perfectCount,
            topPercent = topPercent
        )
        return ResponseEntity.ok(response)
    }
}
