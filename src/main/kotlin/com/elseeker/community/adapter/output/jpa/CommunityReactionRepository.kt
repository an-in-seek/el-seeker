package com.elseeker.community.adapter.output.jpa

import com.elseeker.community.domain.model.CommunityReaction
import com.elseeker.community.domain.vo.ReactionType
import com.elseeker.community.domain.vo.TargetType
import org.springframework.data.jpa.repository.JpaRepository

interface CommunityReactionRepository : JpaRepository<CommunityReaction, Long> {

    fun existsByTargetTypeAndTargetIdAndMemberIdAndReactionType(
        targetType: TargetType,
        targetId: Long,
        memberId: Long,
        reactionType: ReactionType,
    ): Boolean

    fun findByTargetTypeAndTargetIdAndMemberIdAndReactionType(
        targetType: TargetType,
        targetId: Long,
        memberId: Long,
        reactionType: ReactionType,
    ): CommunityReaction?
}
