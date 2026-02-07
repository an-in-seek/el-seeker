package com.elseeker.community.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.community.domain.policy.PostReportPolicy
import com.elseeker.community.domain.vo.PostStatistics
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import com.elseeker.member.domain.model.Member
import com.elseeker.member.domain.vo.MemberRole
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.LanguageCode
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "community_post")
class Post(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: Member,

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 10)
    val type: PostType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val language: LanguageCode,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val country: CountryCode,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false, length = 20)
    var status: PostStatus,

    @Embedded
    var statistics: PostStatistics,

    @Column(nullable = false)
    var isPopular: Boolean,

    @Column(nullable = false)
    val useReply: Boolean,

    @Column(nullable = false)
    val isHtml: Boolean,

    @Column(nullable = false)
    val isWrittenByAdmin: Boolean,

    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
) : BaseTimeEntity(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
) {

    companion object {
        fun create(
            author: Member,
            postType: PostType,
            language: LanguageCode,
            country: CountryCode,
            title: String,
            content: String,
            useReply: Boolean = true,
            isHtml: Boolean = false,
            isWrittenByAdmin: Boolean = false,
        ) = Post(
            author = author,
            type = postType,
            language = language,
            country = country,
            title = title,
            content = content,
            status = PostStatus.PUBLISHED,
            statistics = PostStatistics.create(),
            isPopular = false,
            useReply = useReply,
            isHtml = isHtml,
            isWrittenByAdmin = isWrittenByAdmin,
        )
    }

    fun update(title: String, content: String) {
        this.title = title
        this.content = content
    }

    fun updateBy(actor: Member, title: String, content: String) {
        ensureEditableBy(actor)
        update(title, content)
    }

    fun registerReport(policy: PostReportPolicy = PostReportPolicy) {
        statistics.reportCount += 1
        if (policy.shouldHide(status, statistics.reportCount)) {
            hide()
        }
    }

    fun deleteBy(actor: Member) {
        ensureEditableBy(actor)
        delete()
    }

    fun hide() {
        this.status = PostStatus.HIDDEN
    }

    fun publish() {
        this.status = PostStatus.PUBLISHED
    }

    fun delete() {
        this.status = PostStatus.DELETED
    }

    fun hideByAdmin(actor: Member) {
        ensureAdmin(actor)
        hide()
    }

    fun changeStatusByAdmin(actor: Member, targetStatus: PostStatus) {
        ensureAdmin(actor)
        if (status == targetStatus) return

        when (targetStatus) {
            PostStatus.PUBLISHED -> publish()
            PostStatus.HIDDEN -> {
                if (status == PostStatus.PUBLISHED) {
                    hide()
                }
            }
            PostStatus.DELETED -> delete()
        }
    }

    fun ensureReadableForClient() {
        if (status == PostStatus.HIDDEN) {
            throwError(ErrorType.POST_ACCESS_DENIED, "postId=${id}")
        }
    }

    private fun ensureEditableBy(actor: Member) {
        if (author.id != actor.id && actor.memberRole != MemberRole.ADMIN) {
            throwError(ErrorType.POST_ACCESS_DENIED, "postId=${id}")
        }
    }

    private fun ensureAdmin(actor: Member) {
        if (actor.memberRole != MemberRole.ADMIN) {
            throwError(ErrorType.ADMIN_ACCESS_DENIED)
        }
    }
}
