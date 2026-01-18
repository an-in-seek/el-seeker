package com.elseeker.auth.adapter.input.api.response

import com.elseeker.member.domain.model.Member

data class AuthMeResponse(
    val memberUid: String,
    val email: String,
    val role: String,
    val nickname: String,
    val profileImageUrl: String?,
    val provider: String,
) {
    companion object {
        fun from(member: Member): AuthMeResponse {
            return AuthMeResponse(
                memberUid = member.uid.toString(),
                email = member.email,
                role = member.memberRole.name,
                nickname = member.nickname,
                profileImageUrl = member.profileImageUrl,
                provider = member.provider.registrationId,
            )
        }
    }
}
