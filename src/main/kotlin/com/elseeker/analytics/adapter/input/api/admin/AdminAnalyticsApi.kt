package com.elseeker.analytics.adapter.input.api.admin

import com.elseeker.analytics.adapter.input.api.admin.response.DailyVisitSummaryResponse
import com.elseeker.analytics.adapter.input.api.admin.response.PageVisitStatResponse
import com.elseeker.analytics.adapter.input.api.admin.response.VisitOverviewResponse
import com.elseeker.analytics.application.service.SiteVisitStatsQueryService
import com.elseeker.common.adapter.input.api.admin.response.AdminPageResponse
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/admin/analytics/visitors")
class AdminAnalyticsApi(
    private val siteVisitStatsQueryService: SiteVisitStatsQueryService,
) : AdminAnalyticsApiDocument {

    @GetMapping("/summary")
    override fun getVisitorsSummary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<DailyVisitSummaryResponse> {
        val stats = siteVisitStatsQueryService.getDailyStats(from, to)
        return ResponseEntity.ok(DailyVisitSummaryResponse.from(from, to, stats))
    }

    @GetMapping("/overview")
    override fun getVisitorsOverview(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<VisitOverviewResponse> {
        val overview = siteVisitStatsQueryService.getOverview(from, to)
        return ResponseEntity.ok(VisitOverviewResponse.from(from, to, overview))
    }

    @GetMapping("/pages")
    override fun getVisitorsPages(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @PageableDefault(size = 20) pageable: Pageable,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<AdminPageResponse<PageVisitStatResponse>> {
        val page = siteVisitStatsQueryService.getPageStatsByDate(date, pageable)
        return ResponseEntity.ok(AdminPageResponse.from(page, PageVisitStatResponse::from))
    }
}
