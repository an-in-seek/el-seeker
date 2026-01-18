package com.elseeker.member.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.member.adapter.output.jpa.MemberRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {

    // TODO: 회원(Member) 가입

    // TODO: 회원(Member) 정보 조회

    // TODO: 회원(Member) 정보 수정

    fun deleteMember(memberUid: UUID) {
        val member = memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)
        memberRepository.delete(member)
    }
}
