package com.elseeker.community.application.service

import com.elseeker.community.adapter.input.api.admin.response.AdminReportItem
import com.elseeker.community.adapter.output.jpa.CommunityReportRepository
import com.elseeker.community.domain.vo.TargetType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminCommunityReportService(
    private val communityReportRepository: CommunityReportRepository,
) {

    @Transactional(readOnly = true)
    fun getAdminReports(targetType: TargetType?, pageable: Pageable): Page<AdminReportItem> {
        val normalizedPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize)
        return communityReportRepository.findAdminPage(targetType, normalizedPageable)
    }
}
