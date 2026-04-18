package com.elseeker.analytics.adapter.input.api.admin.response

import com.elseeker.analytics.domain.result.DailyVisitStat
import java.time.LocalDate

data class DailyVisitSummaryResponse(
    val from: LocalDate,
    val to: LocalDate,
    val items: List<Item>,
) {
    data class Item(
        val date: LocalDate,
        val pageViewCount: Long,
        val uniqueVisitorCount: Long,
    ) {
        companion object {
            fun from(stat: DailyVisitStat) = Item(
                date = stat.visitedDate,
                pageViewCount = stat.pageViewCount,
                uniqueVisitorCount = stat.uniqueVisitorCount,
            )
        }
    }

    companion object {
        fun from(from: LocalDate, to: LocalDate, stats: List<DailyVisitStat>) =
            DailyVisitSummaryResponse(
                from = from,
                to = to,
                items = stats.map(Item::from),
            )
    }
}
