package com.elseeker.member.adapter.input.api

import com.elseeker.auth.adapter.input.api.response.AuthMeResponse
import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.member.adapter.input.api.request.MemberOAuthAccountLinkRequest
import com.elseeker.member.adapter.input.api.request.MemberOAuthProfileInitializeRequest
import com.elseeker.member.adapter.input.api.request.MemberUpdateRequest
import com.elseeker.member.adapter.input.api.response.MemberOAuthAccountResponse
import com.elseeker.member.application.service.MemberService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/members")
class MemberApi(
    private val memberService: MemberService
) {

    @PutMapping("/{memberUid}")
    fun updateMember(
        @PathVariable memberUid: UUID,
        @RequestBody request: MemberUpdateRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<AuthMeResponse> {
        val member = memberService.updateMember(
            memberUid = memberUid,
            principalUid = principal.memberUid,
            nickname = request.nickname,
            profileImageUrl = request.profileImageUrl
        )
        return ResponseEntity.ok(AuthMeResponse.from(member))
    }

    @DeleteMapping("/{memberUid}")
    fun deleteMember(
        @PathVariable memberUid: UUID,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<Void> {
        memberService.deleteMember(memberUid, principal.memberUid)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{memberUid}/oauth-accounts")
    fun getOAuthAccounts(
        @PathVariable memberUid: UUID,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<List<MemberOAuthAccountResponse>> {
        val accounts = memberService.getOAuthAccounts(memberUid, principal.memberUid)
        return ResponseEntity.ok(accounts.map(MemberOAuthAccountResponse::from))
    }

    @PostMapping("/{memberUid}/oauth-accounts")
    fun linkOAuthAccount(
        @PathVariable memberUid: UUID,
        @RequestBody request: MemberOAuthAccountLinkRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<AuthMeResponse> {
        val member = memberService.linkOAuthAccount(
            memberUid = memberUid,
            principalUid = principal.memberUid,
            providerRegistrationId = request.provider,
            providerUserId = request.providerUserId
        )
        return ResponseEntity.ok(AuthMeResponse.from(member))
    }

    @DeleteMapping("/{memberUid}/oauth-accounts")
    fun unlinkOAuthAccount(
        @PathVariable memberUid: UUID,
        @RequestParam provider: String,
        @RequestParam providerUserId: String,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<AuthMeResponse> {
        val member = memberService.unlinkOAuthAccount(
            memberUid = memberUid,
            principalUid = principal.memberUid,
            providerRegistrationId = provider,
            providerUserId = providerUserId
        )
        return ResponseEntity.ok(AuthMeResponse.from(member))
    }

    @PostMapping("/{memberUid}/oauth-accounts/initialize-profile")
    fun initializeProfileFromOAuthAccount(
        @PathVariable memberUid: UUID,
        @RequestBody request: MemberOAuthProfileInitializeRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<AuthMeResponse> {
        val member = memberService.initializeProfileFromOAuthAccount(
            memberUid = memberUid,
            principalUid = principal.memberUid,
            providerRegistrationId = request.provider,
            providerUserId = request.providerUserId
        )
        return ResponseEntity.ok(AuthMeResponse.from(member))
    }
}
