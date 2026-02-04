package com.elseeker.community.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.community.domain.vo.PostStatistics
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import com.elseeker.member.domain.model.Member
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.LanguageCode
import jakarta.persistence.*

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
) : BaseTimeEntity(id = id) {

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

    fun hide() {
        this.status = PostStatus.HIDDEN
    }

    fun delete() {
        this.status = PostStatus.DELETED
    }
}