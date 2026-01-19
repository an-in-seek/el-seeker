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
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_uid",
            columnNames = ["uid"]
        ),
        UniqueConstraint(
            name = "uk_member_email",
            columnNames = ["email"]
        ),
        UniqueConstraint(
            name = "uk_member_provider_user",
            columnNames = ["provider", "provider_user_id"]
        )
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

    fun update(
        nickname: String,
        profileImageUrl: String?
    ) {
        val trimmed = nickname.trim()
        if (trimmed.isBlank()) {
            throwError(ErrorType.REQUIRED_NICKNAME)
        }
        if (trimmed.contains(' ')) {
            throwError(ErrorType.INVALID_NICKNAME_FORMAT)
        }
        this.nickname = nickname.trim()
        profileImageUrl?.let { this.profileImageUrl = it }
    }


    /**
     * [도메인 비즈니스 메서드] OAuth 정보 동기화
     */
    fun syncWithOAuth(
        inputProvider: OAuthProvider,
        inputProviderUserId: String,
        newNickname: String,
        newProfileImageUrl: String?
    ) {
        if (this.provider != inputProvider) {
            throwError(ErrorType.PROVIDER_MISMATCH, inputProvider.registrationId)
        }
        if (this.providerUserId != inputProviderUserId) {
            throwError(ErrorType.PROVIDER_USER_ID_MISMATCH, inputProvider.registrationId)
        }
        if (newNickname.isNotBlank() && this.nickname.isBlank()) {
            this.nickname = newNickname
        }
        if (newProfileImageUrl != null && this.profileImageUrl.isNullOrBlank()) {
            this.profileImageUrl = newProfileImageUrl
        }
    }

}
