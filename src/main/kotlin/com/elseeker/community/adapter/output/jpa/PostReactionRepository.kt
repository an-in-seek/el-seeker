package com.elseeker.community.adapter.output.jpa

import com.elseeker.community.domain.model.PostReaction
import com.elseeker.community.domain.vo.ReactionType
import org.springframework.data.jpa.repository.JpaRepository

interface PostReactionRepository : JpaRepository<PostReaction, Long> {

    fun findByPostIdAndMemberIdAndType(postId: Long, memberId: Long, type: ReactionType): PostReaction?

    fun existsByPostIdAndMemberIdAndType(postId: Long, memberId: Long, type: ReactionType): Boolean
}
