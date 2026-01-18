package com.elseeker.member.adapter.input.api

import com.elseeker.member.application.service.MemberService
import com.elseeker.common.security.jwt.JwtPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/members")
class MemberApiController(
    private val memberService: MemberService
) {

    @DeleteMapping("/{memberUid}")
    fun deleteMember(
        @PathVariable memberUid: java.util.UUID,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<Void> {
        memberService.deleteMember(memberUid, principal.memberUid)
        return ResponseEntity.noContent().build()
    }
}
