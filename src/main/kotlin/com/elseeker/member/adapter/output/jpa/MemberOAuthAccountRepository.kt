package com.elseeker.member.adapter.output.jpa

import com.elseeker.member.domain.model.MemberOAuthAccount
import com.elseeker.member.domain.vo.OAuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MemberOAuthAccountRepository : JpaRepository<MemberOAuthAccount, Long> {
    fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String
    ): MemberOAuthAccount?

    fun findAllByMemberUid(memberUid: UUID): List<MemberOAuthAccount>
}
