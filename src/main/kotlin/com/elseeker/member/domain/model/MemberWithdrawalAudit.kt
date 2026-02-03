package com.elseeker.member.domain.model

import com.elseeker.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(name = "member_withdraw_audit")
class MemberWithdrawalAudit(

    id: Long? = null,

    @Column(name = "member_uid", nullable = false)
    val memberUid: UUID,

    @Column(nullable = false, length = 255)
    val email: String,

    @Column(nullable = false, length = 50)
    val nickname: String,

    @Column(name = "deleted_at", nullable = false)
    val deletedAt: Instant = Instant.now()
) : BaseEntity(
    id = id,
)
