package com.elseeker.analytics.application.service

import com.elseeker.analytics.adapter.output.jpa.SiteVisitEventRepository
import com.elseeker.analytics.domain.result.DailyVisitStat
import com.elseeker.analytics.domain.result.PageVisitStat
import com.elseeker.analytics.domain.result.VisitOverviewStat
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
@Transactional(readOnly = true)
class SiteVisitStatsQueryService(
    private val siteVisitEventRepository: SiteVisitEventRepository,
) {

    fun getDailyStats(fromDate: LocalDate, toDate: LocalDate): List<DailyVisitStat> {
        validateDateRange(fromDate, toDate)
        return siteVisitEventRepository.findDailyStats(fromDate, toDate)
    }

    fun getPageStatsByDate(date: LocalDate, pageable: Pageable): Page<PageVisitStat> {
        val unsorted = PageRequest.of(pageable.pageNumber, pageable.pageSize)
        return siteVisitEventRepository.findPageStatsByDate(date, unsorted)
    }

    fun getOverview(fromDate: LocalDate, toDate: LocalDate): VisitOverviewStat {
        validateDateRange(fromDate, toDate)
        return siteVisitEventRepository.findOverview(fromDate, toDate)
    }

    private fun validateDateRange(fromDate: LocalDate, toDate: LocalDate) {
        if (fromDate.isAfter(toDate)) {
            throwError(ErrorType.INVALID_PARAMETER)
        }
        if (ChronoUnit.DAYS.between(fromDate, toDate) > MAX_RANGE_DAYS) {
            throwError(ErrorType.INVALID_PARAMETER)
        }
    }

    companion object {
        private const val MAX_RANGE_DAYS = 366L
    }
}
