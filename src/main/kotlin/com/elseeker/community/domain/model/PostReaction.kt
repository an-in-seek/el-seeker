package com.elseeker.community.domain.model

import com.elseeker.common.domain.BaseEntity
import com.elseeker.community.domain.vo.ReactionType
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "community_post_reaction",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_post_reaction_member_type",
            columnNames = ["post_id", "member_id", "reaction_type"]
        )
    ],
    indexes = [
        Index(
            name = "idx_post_reaction_member_created_at",
            columnList = "member_id, created_at"
        )
    ],
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

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
) : BaseEntity(id = id) {

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
