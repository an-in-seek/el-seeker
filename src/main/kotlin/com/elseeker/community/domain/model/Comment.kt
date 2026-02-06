package com.elseeker.community.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*

@Entity
@Table(name = "community_comment")
class Comment(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: Member,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false)
    var deleted: Boolean = false,

) : BaseTimeEntity(id = id) {

    companion object {
        fun create(
            post: Post,
            author: Member,
            content: String,
        ) = Comment(
            post = post,
            author = author,
            content = content,
        )
    }

    fun softDelete() {
        this.deleted = true
        this.content = ""
    }
}
