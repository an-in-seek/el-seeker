package com.elseeker.member.adapter.output.jpa

import com.elseeker.member.domain.model.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?
    fun findByUid(uid: UUID): Member?

    @Query(
        """
        SELECT member
        FROM Member member
        LEFT JOIN FETCH member.oauthAccounts
        WHERE member.uid = :uid
        """
    )
    fun findWithOAuthAccountsByUid(@Param("uid") uid: UUID): Member?
}
