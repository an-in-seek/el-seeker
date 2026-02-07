package com.elseeker.community.domain.model

import com.elseeker.common.domain.BaseEntity
import com.elseeker.community.domain.vo.ReportReason
import com.elseeker.community.domain.vo.TargetType
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "community_report",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_report_unique",
            columnNames = ["target_type", "target_id", "reporter_id"]
        )
    ],
    indexes = [
        Index(
            name = "idx_report_target",
            columnList = "target_type, target_id"
        )
    ],
)
class CommunityReport(

    id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    val targetType: TargetType,

    @Column(name = "target_id", nullable = false)
    val targetId: Long,

    @Column(name = "reporter_id", nullable = false)
    val reporterId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 20)
    val reason: ReportReason,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
) : BaseEntity(id = id) {

    companion object {
        fun create(
            targetType: TargetType,
            targetId: Long,
            reporterId: Long,
            reason: ReportReason,
        ) = CommunityReport(
            targetType = targetType,
            targetId = targetId,
            reporterId = reporterId,
            reason = reason,
        )
    }
}
