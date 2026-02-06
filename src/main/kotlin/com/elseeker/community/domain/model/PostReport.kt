package com.elseeker.community.domain.model

import com.elseeker.common.domain.BaseEntity
import com.elseeker.community.domain.vo.ReportReason
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "community_post_report",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_report_member",
            columnNames = ["post_id", "reporter_id"]
        )
    ],
    indexes = [
        Index(
            name = "idx_post_report_reporter_created_at",
            columnList = "reporter_id, created_at"
        )
    ],
)
class PostReport(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

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
            post: Post,
            reporter: Member,
            reason: ReportReason,
        ) = PostReport(
            post = post,
            reporter = reporter,
            reason = reason,
        )
    }
}
