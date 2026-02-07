package com.elseeker.community.adapter.output.jpa

import com.elseeker.community.domain.model.CommunityReport
import com.elseeker.community.domain.vo.TargetType
import org.springframework.data.jpa.repository.JpaRepository

interface CommunityReportRepository : JpaRepository<CommunityReport, Long> {

    fun existsByTargetTypeAndTargetIdAndReporterId(
        targetType: TargetType,
        targetId: Long,
        reporterId: Long,
    ): Boolean
}
