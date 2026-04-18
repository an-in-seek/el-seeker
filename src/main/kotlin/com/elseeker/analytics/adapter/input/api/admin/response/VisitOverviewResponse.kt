package com.elseeker.analytics.adapter.input.api.admin.response

import com.elseeker.analytics.domain.result.VisitOverviewStat
import java.time.LocalDate

data class VisitOverviewResponse(
    val from: LocalDate,
    val to: LocalDate,
    val totalPageViewCount: Long,
    val periodUniqueVisitorCount: Long,
    val periodAuthenticatedUniqueMemberCount: Long,
) {
    companion object {
        fun from(from: LocalDate, to: LocalDate, stat: VisitOverviewStat) =
            VisitOverviewResponse(
                from = from,
                to = to,
                totalPageViewCount = stat.totalPageViewCount,
                periodUniqueVisitorCount = stat.periodUniqueVisitorCount,
                periodAuthenticatedUniqueMemberCount = stat.periodAuthenticatedUniqueMemberCount,
            )
    }
}
