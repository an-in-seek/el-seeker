package com.elseeker.community.domain.model

import com.elseeker.common.domain.BaseEntity
import com.elseeker.community.domain.vo.ReportReason
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "community_comment_report",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_comment_report_member",
            columnNames = ["comment_id", "reporter_id"]
        )
    ],
    indexes = [
        Index(
            name = "idx_comment_report_reporter_created_at",
            columnList = "reporter_id, created_at"
        )
    ],
)
class CommentReport(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    val comment: Comment,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    val reporter: Member,

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 20)
    val reason: ReportReason,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
) : BaseEntity(id = id) {

    companion object {
        fun create(
            comment: Comment,
            reporter: Member,
            reason: ReportReason,
        ) = CommentReport(
            comment = comment,
            reporter = reporter,
            reason = reason,
        )
    }
}
