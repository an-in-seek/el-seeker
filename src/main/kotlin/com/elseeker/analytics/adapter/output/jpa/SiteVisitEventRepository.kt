package com.elseeker.analytics.adapter.output.jpa

import com.elseeker.analytics.domain.model.SiteVisitEvent
import com.elseeker.analytics.domain.result.DailyVisitStat
import com.elseeker.analytics.domain.result.PageVisitStat
import com.elseeker.analytics.domain.result.VisitOverviewStat
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface SiteVisitEventRepository : JpaRepository<SiteVisitEvent, Long> {

    @Query(
        """
        SELECT new com.elseeker.analytics.domain.result.DailyVisitStat(
            e.visitedDate,
            COUNT(e),
            COUNT(DISTINCT e.visitorId)
        )
        FROM SiteVisitEvent e
        WHERE e.visitedDate BETWEEN :fromDate AND :toDate
          AND e.isBot = false
        GROUP BY e.visitedDate
        ORDER BY e.visitedDate ASC
        """
    )
    fun findDailyStats(
        @Param("fromDate") fromDate: LocalDate,
        @Param("toDate") toDate: LocalDate,
    ): List<DailyVisitStat>

    @Query(
        value = """
        SELECT new com.elseeker.analytics.domain.result.PageVisitStat(
            e.pageKey,
            COUNT(e),
            COUNT(DISTINCT e.visitorId)
        )
        FROM SiteVisitEvent e
        WHERE e.visitedDate = :date
          AND e.isBot = false
        GROUP BY e.pageKey
        ORDER BY COUNT(e) DESC, e.pageKey ASC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT e.pageKey)
        FROM SiteVisitEvent e
        WHERE e.visitedDate = :date
          AND e.isBot = false
        """
    )
    fun findPageStatsByDate(
        @Param("date") date: LocalDate,
        pageable: Pageable,
    ): Page<PageVisitStat>

    @Query(
        """
        SELECT new com.elseeker.analytics.domain.result.VisitOverviewStat(
            COUNT(e),
            COUNT(DISTINCT e.visitorId),
            COUNT(DISTINCT CASE WHEN e.isAuthenticated = true THEN e.memberUid END)
        )
        FROM SiteVisitEvent e
        WHERE e.visitedDate BETWEEN :fromDate AND :toDate
          AND e.isBot = false
        """
    )
    fun findOverview(
        @Param("fromDate") fromDate: LocalDate,
        @Param("toDate") toDate: LocalDate,
    ): VisitOverviewStat
}
