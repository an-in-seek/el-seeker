package com.elseeker.member.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.model.Member
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {

    // TODO: 회원(Member) 가입

    // TODO: 회원(Member) 정보 조회

    // TODO: 회원(Member) 정보 수정

    fun getMember(memberUid: UUID) = memberRepository.findByUid(memberUid)
        ?: throwError(ErrorType.MEMBER_NOT_FOUND, memberUid)

    fun deleteMember(memberUid: UUID, principalUid: UUID) {
        if (memberUid != principalUid) {
            throwError(ErrorType.MEMBER_ACCESS_DENIED, memberUid)
        }
        val member = getMember(memberUid)
        memberRepository.delete(member)
    }

    fun updateMember(memberUid: UUID, principalUid: UUID, nickname: String, profileImageUrl: String?): Member {
        if (memberUid != principalUid) {
            throwError(ErrorType.MEMBER_ACCESS_DENIED, memberUid)
        }
        val trimmedNickname = nickname.trim()
        if (trimmedNickname.isBlank()) {
            throwError(ErrorType.INVALID_PARAMETER, "nickname")
        }
        val member = getMember(memberUid)
        member.nickname = trimmedNickname
        member.profileImageUrl = profileImageUrl?.trim()?.ifBlank { null }
        return memberRepository.save(member)
    }
}
