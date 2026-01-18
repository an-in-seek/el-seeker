package com.elseeker.member.adapter.output.jpa

import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?
    fun findByUid(uid: UUID): Member?
}
