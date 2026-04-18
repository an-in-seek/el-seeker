package com.elseeker.analytics.domain.result

data class PageVisitStat(
    val pageKey: String,
    val pageViewCount: Long,
    val uniqueVisitorCount: Long,
)
