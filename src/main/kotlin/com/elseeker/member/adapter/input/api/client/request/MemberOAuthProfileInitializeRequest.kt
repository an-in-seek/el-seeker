package com.elseeker.member.adapter.input.api.client.request

data class MemberOAuthProfileInitializeRequest(
    val provider: String,
    val providerUserId: String
)
