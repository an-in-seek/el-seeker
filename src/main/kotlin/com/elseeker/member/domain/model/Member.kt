package com.elseeker.member.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.member.domain.vo.MemberRole
import com.elseeker.member.domain.vo.OAuthProvider
import com.elseeker.member.domain.vo.OAuthProviderConverter
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@Table(
    name = "member",
    indexes = [
        Index(name = "idx_member_email", columnList = "email"),
        Index(name = "idx_member_uuid", columnList = "uuid")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Member(

    @Column(nullable = false, unique = true, columnDefinition = "uuid default gen_random_uuid()")
    var uid: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 255, unique = true)
    var email: String,

    @Column(nullable = false, length = 100)
    var nickname: String = "",

    @Column(length = 512)
    var profileImageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var memberRole: MemberRole = MemberRole.USER,

    // 소셜 플랫폼 이름 (google, naver, kakao)
    @Convert(converter = OAuthProviderConverter::class)
    @Column(nullable = false, length = 50)
    var provider: OAuthProvider,

    // 공급자가 부여한 사용자 ID
    @Column(nullable = false, length = 255)
    var providerUserId: String

) : BaseTimeEntity() {

    companion object {

        fun create(
            email: String,
            nickname: String,
            profileImageUrl: String?,
            memberRole: MemberRole,
            provider: OAuthProvider,
            providerUserId: String
        ) = Member(
            email = email,
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            memberRole = memberRole,
            provider = provider,
            providerUserId = providerUserId
        )
    }

    /**
     * [도메인 비즈니스 메서드] OAuth 정보 동기화
     * - 책임 1: 공급자(Provider) 일치 여부 검증 (Invariants)
     * - 책임 2: 프로필 정보 업데이트 (State Change)
     */
    fun syncWithOAuth(
        inputProvider: OAuthProvider,
        inputProviderUserId: String,
        newNickname: String,
        newProfileImageUrl: String?
    ) {
        // 1. 공급자 일치 여부 검증
        if (this.provider != inputProvider) {
            throwError(ErrorType.PROVIDER_MISMATCH, inputProvider.registrationId)
        }
        if (this.providerUserId != inputProviderUserId) {
            throwError(ErrorType.PROVIDER_USER_ID_MISMATCH, inputProvider.registrationId)
        }

        // 2. 닉네임이 비어있지 않을 때만 업데이트 (선택 사항, 빈값 방지)
        if (newNickname.isNotBlank()) {
            this.nickname = newNickname
        }

        // 3. 프로필 이미지가 넘어왔을 때만 업데이트 (기존 이미지 보존)
        if (newProfileImageUrl != null) {
            this.profileImageUrl = newProfileImageUrl
        }
    }

}
