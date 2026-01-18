package com.elseeker.auth.adapter.input.api.response

import com.elseeker.member.domain.model.Member

data class AuthMeResponse(
    val memberUid: String,
    val email: String,
    val role: String,
) {
    companion object {
        fun from(member: Member): AuthMeResponse {
            return AuthMeResponse(
                memberUid = member.uid.toString(),
                email = member.email,
                role = member.memberRole.name,
            )
        }
    }
}
