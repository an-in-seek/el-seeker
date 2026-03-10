package com.elseeker.game.adapter.input.api.client

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.game.adapter.input.api.client.response.GameRankingListResponse
import com.elseeker.game.adapter.input.api.client.response.MyRankingDetailResponse
import com.elseeker.game.domain.vo.GameType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "Game Ranking", description = "게임 랭킹 API")
interface GameRankingApiDocument {

    @Operation(summary = "게임별 랭킹 목록 조회", description = "게임 유형별 상위 N명의 랭킹을 조회합니다. 로그인 시 내 순위도 포함됩니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 게임 유형")
    )
    fun getRankings(
        @Parameter(description = "게임 유형", example = "OX_QUIZ") gameType: GameType,
        @Parameter(description = "조회 인원 수 (최대 100)", example = "50") limit: Int,
        @Parameter(hidden = true) principal: JwtPrincipal?
    ): ResponseEntity<GameRankingListResponse>

    @Operation(summary = "내 순위 조회", description = "로그인 사용자의 특정 게임 순위를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "랭킹 데이터 없음")
    )
    fun getMyRanking(
        @Parameter(description = "게임 유형", example = "OX_QUIZ") gameType: GameType,
        @Parameter(hidden = true) principal: JwtPrincipal
    ): ResponseEntity<MyRankingDetailResponse>
}
