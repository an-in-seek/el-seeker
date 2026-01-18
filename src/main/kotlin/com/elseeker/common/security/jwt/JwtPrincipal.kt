package com.elseeker.common.security.jwt

import com.elseeker.member.domain.vo.MemberRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

data class JwtPrincipal(
    val memberUid: UUID,
    val email: String,
    val roles: List<MemberRole>
) {
    // String 기반 권한을 Spring Security 표준 GrantedAuthority로 변환
    fun getAuthorities(): Collection<GrantedAuthority> {
        return roles.map { SimpleGrantedAuthority(it.key) }
    }
}
