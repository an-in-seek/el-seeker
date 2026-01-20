package com.elseeker.member.adapter.input.api.request

data class MemberOAuthProfileInitializeRequest(
    val provider: String,
    val providerUserId: String
)
