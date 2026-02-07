package com.elseeker.community.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.community.adapter.output.jpa.CommunityReactionRepository
import com.elseeker.community.adapter.output.jpa.PostRepository
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
        val post = postRepository.findByIdAndStatusNotForUpdate(postId, PostStatus.DELETED) ?: throwError(ErrorType.POST_NOT_FOUND)
        val memberId = getMemberIdOrThrow(memberUid)
        if (communityReactionRepository.existsByTargetTypeAndTargetIdAndMemberIdAndReactionType(TargetType.POST, postId, memberId, type)) {
            throwError(ErrorType.REACTION_ALREADY_EXISTS)
        }
        val reaction = CommunityReaction.create(
            targetType = TargetType.POST,
            targetId = postId,
            memberId = memberId,
            reactionType = type,
        )
        communityReactionRepository.save(reaction)
        post.addReaction()
    }

    @Transactional
    fun removeReaction(postId: Long, memberUid: UUID, type: ReactionType) {
        val post = postRepository.findByIdAndStatusNotForUpdate(postId, PostStatus.DELETED) ?: throwError(ErrorType.POST_NOT_FOUND)
        val memberId = getMemberIdOrThrow(memberUid)
        val reaction = communityReactionRepository.findByTargetTypeAndTargetIdAndMemberIdAndReactionType(TargetType.POST, postId, memberId, type)
            ?: throwError(ErrorType.REACTION_NOT_FOUND)
        communityReactionRepository.delete(reaction)
        post.removeReaction()
    }

    private fun getMemberIdOrThrow(memberUid: UUID): Long {
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        return member.id ?: throwError(ErrorType.MEMBER_NOT_FOUND)
    }

}
