package com.elseeker.auth.adapter.input.api

import com.elseeker.auth.adapter.input.api.response.AuthMeResponse
import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.member.application.service.MemberService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthApi(
    private val memberService: MemberService
) {

    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): AuthMeResponse {
        val member = memberService.getMemberWithOAuthAccounts(principal.memberUid)
        return AuthMeResponse.from(member)
    }
}
