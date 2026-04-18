package com.elseeker.analytics.domain.result

import java.time.LocalDate

data class DailyVisitStat(
    val visitedDate: LocalDate,
    val pageViewCount: Long,
    val uniqueVisitorCount: Long,
)
