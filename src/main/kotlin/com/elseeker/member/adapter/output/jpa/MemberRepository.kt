package com.elseeker.member.adapter.output.jpa

import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?
}