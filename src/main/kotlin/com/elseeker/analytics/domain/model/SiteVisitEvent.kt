package com.elseeker.analytics.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(
    name = "site_visit_event",
    indexes = [
        Index(name = "idx_site_visit_event_visited_date", columnList = "visited_date"),
        Index(name = "idx_site_visit_event_visited_date_page_key", columnList = "visited_date, page_key"),
        Index(name = "idx_site_visit_event_visited_date_visitor_id", columnList = "visited_date, visitor_id"),
        Index(name = "idx_site_visit_event_member_uid_visited_date", columnList = "member_uid, visited_date"),
    ]
)
@EntityListeners(AuditingEntityListener::class)
class SiteVisitEvent(

    id: Long? = null,

    @Column(name = "visitor_id", nullable = false, length = 36)
    val visitorId: String,

    @Column(name = "member_uid")
    val memberUid: UUID? = null,

    @Column(name = "page_key", nullable = false, length = 160)
    val pageKey: String,

    @Column(name = "request_uri", nullable = false, length = 255)
    val requestUri: String,

    @Column(name = "referer_host", length = 120)
    val refererHost: String? = null,

    @Column(name = "is_authenticated", nullable = false)
    val isAuthenticated: Boolean = false,

    @Column(name = "is_bot", nullable = false)
    val isBot: Boolean = false,

    @Column(name = "visited_at", nullable = false)
    val visitedAt: Instant,

    @Column(name = "visited_date", nullable = false)
    val visitedDate: LocalDate,

) : BaseTimeEntity(id = id)
