package com.elseeker.analytics.adapter.input.api.admin.response

import com.elseeker.analytics.domain.result.PageVisitStat

data class PageVisitStatResponse(
    val pageKey: String,
    val pageViewCount: Long,
    val uniqueVisitorCount: Long,
) {
    companion object {
        fun from(stat: PageVisitStat) = PageVisitStatResponse(
            pageKey = stat.pageKey,
            pageViewCount = stat.pageViewCount,
            uniqueVisitorCount = stat.uniqueVisitorCount,
        )
    }
}
