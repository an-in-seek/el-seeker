package com.elseeker.member.adapter.input.api.response

import com.elseeker.member.domain.model.MemberOAuthAccount
import java.time.Instant

data class MemberOAuthAccountResponse(
    val provider: String,
    val providerUserId: String,
    val email: String?,
    val nickname: String?,
    val createdAt: Instant
) {
    companion object {
        fun from(account: MemberOAuthAccount) = MemberOAuthAccountResponse(
            provider = account.provider.registrationId,
            providerUserId = account.providerUserId,
            email = account.email,
            nickname = account.nickname,
            createdAt = account.createdAt
        )
    }
}
