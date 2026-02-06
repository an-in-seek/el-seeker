package com.elseeker.community.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.community.domain.vo.ReactionType
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*

@Entity
@Table(
    name = "community_post_reaction",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_reaction_member_type",
            columnNames = ["post_id", "member_id", "reaction_type"]
        )
    ]
)
class PostReaction(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 10)
    val type: ReactionType,

) : BaseTimeEntity(id = id) {

    companion object {
        fun create(
            post: Post,
            member: Member,
            type: ReactionType,
        ) = PostReaction(
            post = post,
            member = member,
            type = type,
        )
    }
}
