package com.elseeker.community.domain.model

import com.elseeker.common.domain.BaseEntity
import com.elseeker.community.domain.vo.ReactionType
import com.elseeker.community.domain.vo.TargetType
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "community_reaction",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_reaction_unique",
            columnNames = ["target_type", "target_id", "member_id", "reaction_type"]
        )
    ],
    indexes = [
        Index(
            name = "idx_reaction_target",
            columnList = "target_type, target_id"
        )
    ],
)
class CommunityReaction(

    id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    val targetType: TargetType,

    @Column(name = "target_id", nullable = false)
    val targetId: Long,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 10)
    val reactionType: ReactionType,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
) : BaseEntity(id = id) {

    companion object {
        fun create(
            targetType: TargetType,
            targetId: Long,
            memberId: Long,
            reactionType: ReactionType,
        ) = CommunityReaction(
            targetType = targetType,
            targetId = targetId,
            memberId = memberId,
            reactionType = reactionType,
        )
    }
}
