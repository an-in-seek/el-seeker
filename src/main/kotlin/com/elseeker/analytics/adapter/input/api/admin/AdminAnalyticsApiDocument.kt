package com.elseeker.analytics.adapter.input.api.admin

import com.elseeker.analytics.adapter.input.api.admin.response.DailyVisitSummaryResponse
import com.elseeker.analytics.adapter.input.api.admin.response.PageVisitStatResponse
import com.elseeker.analytics.adapter.input.api.admin.response.VisitOverviewResponse
import com.elseeker.common.adapter.input.api.admin.response.AdminPageResponse
import com.elseeker.common.security.jwt.JwtPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import java.time.LocalDate

@Tag(name = "Analytics Admin", description = "사이트 방문 집계 관리자 API")
interface AdminAnalyticsApiDocument {

    @Operation(summary = "일별 방문 요약 조회", description = "기간 내 일별 페이지뷰/고유 방문자 수를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun getVisitorsSummary(
        @Parameter(description = "시작 날짜 (KST)") from: LocalDate,
        @Parameter(description = "종료 날짜 (KST)") to: LocalDate,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<DailyVisitSummaryResponse>

    @Operation(summary = "기간 KPI 요약 조회", description = "기간 내 전체 페이지뷰, 기간 고유 방문자, 회원 고유 방문자 수를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun getVisitorsOverview(
        @Parameter(description = "시작 날짜 (KST)") from: LocalDate,
        @Parameter(description = "종료 날짜 (KST)") to: LocalDate,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<VisitOverviewResponse>

    @Operation(summary = "특정 날짜 페이지별 방문 통계", description = "지정한 날짜의 페이지 키별 방문 통계를 페이지네이션으로 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun getVisitorsPages(
        @Parameter(description = "조회 날짜 (KST)") date: LocalDate,
        pageable: Pageable,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<AdminPageResponse<PageVisitStatResponse>>
}
