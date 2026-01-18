package com.elseeker.member.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.member.application.service.MemberService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/members")
class MemberApiController(
    private val memberService: MemberService
) {

    @DeleteMapping("/{memberUid}")
    fun deleteMember(
        @PathVariable memberUid: String,
        authentication: Authentication?
    ): ResponseEntity<Void> {
        val principal = authentication?.principal
        if (authentication == null || !authentication.isAuthenticated || principal !is JwtPrincipal) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val memberUuid = runCatching { UUID.fromString(memberUid) }.getOrNull()
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()

        if (principal.memberUid != memberUuid.toString()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        memberService.deleteMember(memberUuid)
        return ResponseEntity.noContent().build()
    }
}
