package com.elseeker.member.adapter.input.api.request

data class MemberOAuthAccountLinkRequest(
    val provider: String,
    val providerUserId: String
)
