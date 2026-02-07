package com.elseeker.community.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.community.adapter.output.jpa.PostRepository
import com.elseeker.community.adapter.output.jpa.CommunityReactionRepository
import com.elseeker.community.domain.model.CommunityReaction
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.ReactionType
import com.elseeker.community.domain.vo.TargetType
import com.elseeker.member.adapter.output.jpa.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ReactionService(
    private val communityReactionRepository: CommunityReactionRepository,
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository,
) {

    @Transactional
    fun addReaction(postId: Long, memberUid: UUID, type: ReactionType) {
        val post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED)
            ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")

        val member = memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND)

        val memberId = requireNotNull(member.id)

        if (communityReactionRepository.existsByTargetTypeAndTargetIdAndMemberIdAndReactionType(
                TargetType.POST,
                postId,
                memberId,
                type,
            )
        ) {
            throwError(ErrorType.REACTION_ALREADY_EXISTS, "postId=$postId", "type=$type")
        }

        val reaction = CommunityReaction.create(
            targetType = TargetType.POST,
            targetId = postId,
            memberId = memberId,
            reactionType = type,
        )
        communityReactionRepository.save(reaction)

        postRepository.incrementReactionCount(postId)
        postRepository.updateScore(postId)
    }

    @Transactional
    fun removeReaction(postId: Long, memberUid: UUID, type: ReactionType) {
        val member = memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND)

        val memberId = requireNotNull(member.id)

        val reaction = communityReactionRepository.findByTargetTypeAndTargetIdAndMemberIdAndReactionType(
            TargetType.POST,
            postId,
            memberId,
            type,
        )
            ?: throwError(ErrorType.REACTION_NOT_FOUND, "postId=$postId", "type=$type")

        communityReactionRepository.delete(reaction)

        postRepository.decrementReactionCount(postId)
        postRepository.updateScore(postId)
    }
}
