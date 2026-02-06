package com.elseeker.community.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.community.domain.vo.CommentStatus
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "community_comment",
    indexes = [
        Index(
            name = "idx_comment_post_created_at",
            columnList = "post_id, created_at"
        ),
        Index(
            name = "idx_comment_author_created_at",
            columnList = "author_id, created_at"
        )
    ]
)
class Comment(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: Member,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CommentStatus,

    @Column(name = "report_count", nullable = false)
    var reportCount: Long = 0,

    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
) : BaseTimeEntity(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
) {

    companion object {
        fun create(
            post: Post,
            author: Member,
            content: String,
        ) = Comment(
            post = post,
            author = author,
            content = content,
            status = CommentStatus.PUBLISHED,
        )
    }

    fun update(content: String) {
        this.content = content
    }

    fun delete() {
        this.status = CommentStatus.DELETED
    }

    fun hide() {
        this.status = CommentStatus.HIDDEN
    }
}
