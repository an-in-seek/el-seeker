package com.elseeker.member.adapter.input.api.client.request

data class MemberOAuthAccountLinkRequest(
    val provider: String,
    val providerUserId: String
)
