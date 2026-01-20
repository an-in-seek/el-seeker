package com.elseeker.member.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.vo.OAuthProvider
import com.elseeker.member.domain.vo.OAuthProviderConverter
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "member_oauth_account",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_oauth_provider_user",
            columnNames = ["provider", "provider_user_id"]
        )
    ]
)
class MemberOAuthAccount(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @Convert(converter = OAuthProviderConverter::class)
    @Column(nullable = false, length = 50)
    var provider: OAuthProvider,

    @Column(name = "provider_user_id", nullable = false, length = 255)
    var providerUserId: String,

    @Column(name = "email", length = 255)
    var email: String? = null,

    @Column(name = "nickname", length = 50)
    var nickname: String? = null,

    @Column(name = "profile_image_url", length = 512)
    var profileImageUrl: String? = null,

    @Column(name = "last_synced_at")
    var lastSyncedAt: LocalDateTime? = null

) : BaseTimeEntity() {

    companion object {
        fun create(
            member: Member,
            provider: OAuthProvider,
            providerUserId: String,
            email: String?,
            nickname: String?,
            profileImageUrl: String?
        ) = MemberOAuthAccount(
            member = member,
            provider = provider,
            providerUserId = providerUserId,
            email = email,
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            lastSyncedAt = LocalDateTime.now()
        )
    }

    fun syncOAuthProfile(
        email: String?,
        nickname: String?,
        profileImageUrl: String?
    ) {
        this.email = email
        this.nickname = nickname
        this.profileImageUrl = profileImageUrl
        this.lastSyncedAt = LocalDateTime.now()
    }
}
