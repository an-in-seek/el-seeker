package com.elseeker.analytics.domain.result

data class VisitOverviewStat(
    val totalPageViewCount: Long,
    val periodUniqueVisitorCount: Long,
    val periodAuthenticatedUniqueMemberCount: Long,
)
