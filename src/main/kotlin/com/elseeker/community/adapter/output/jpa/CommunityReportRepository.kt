package com.elseeker.community.adapter.output.jpa

import com.elseeker.community.domain.model.CommunityReport
import com.elseeker.community.domain.vo.TargetType
import com.elseeker.community.adapter.input.api.admin.response.AdminReportItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommunityReportRepository : JpaRepository<CommunityReport, Long> {

    fun existsByTargetTypeAndTargetIdAndReporterId(
        targetType: TargetType,
        targetId: Long,
        reporterId: Long,
    ): Boolean

    @Query(
        value = """
        SELECT new com.elseeker.community.adapter.input.api.admin.response.AdminReportItem(
            r.id,
            r.targetType,
            r.targetId,
            r.reason,
            r.reporterId,
            m.nickname,
            r.createdAt
        )
        FROM CommunityReport r
        LEFT JOIN Member m ON m.id = r.reporterId
        WHERE (:targetType IS NULL OR r.targetType = :targetType)
        ORDER BY r.createdAt DESC
        """,
        countQuery = """
        SELECT count(r) FROM CommunityReport r
        WHERE (:targetType IS NULL OR r.targetType = :targetType)
        """
    )
    fun findAdminPage(
        @Param("targetType") targetType: TargetType?,
        pageable: Pageable,
    ): Page<AdminReportItem>
}
