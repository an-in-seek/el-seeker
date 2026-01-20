package com.elseeker.member.adapter.output.jpa

import com.elseeker.member.domain.model.MemberWithdrawalAudit
import org.springframework.data.jpa.repository.JpaRepository

interface MemberWithdrawalAuditRepository : JpaRepository<MemberWithdrawalAudit, Long>
