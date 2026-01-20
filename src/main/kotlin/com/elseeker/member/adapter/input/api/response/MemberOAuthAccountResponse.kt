package com.elseeker.member.adapter.input.api.response

import com.elseeker.member.domain.model.MemberOAuthAccount
import java.time.LocalDateTime

data class MemberOAuthAccountResponse(
    val provider: String,
    val providerUserId: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(account: MemberOAuthAccount) = MemberOAuthAccountResponse(
            provider = account.provider.registrationId,
            providerUserId = account.providerUserId,
            createdAt = account.createdAt
        )
    }
}
