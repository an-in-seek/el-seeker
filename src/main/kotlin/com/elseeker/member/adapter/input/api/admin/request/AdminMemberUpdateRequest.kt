package com.elseeker.member.adapter.input.api.admin.request

import com.elseeker.member.domain.vo.MemberRole

data class AdminMemberUpdateRequest(
    val nickname: String,
    val profileImageUrl: String?,
    val memberRole: MemberRole
)
