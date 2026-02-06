package com.elseeker.community.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.community.adapter.output.jpa.PostReactionRepository
import com.elseeker.community.adapter.output.jpa.PostRepository
import com.elseeker.community.domain.model.PostReaction
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.ReactionType
import com.elseeker.member.adapter.output.jpa.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ReactionService(
    private val postReactionRepository: PostReactionRepository,
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

        if (postReactionRepository.existsByPostIdAndMemberIdAndType(postId, memberId, type)) {
            throwError(ErrorType.REACTION_ALREADY_EXISTS, "postId=$postId", "type=$type")
        }

        val reaction = PostReaction.create(
            post = post,
            member = member,
            type = type,
        )
        postReactionRepository.save(reaction)

        postRepository.incrementReactionCount(postId)
        postRepository.updateScore(postId)
    }

    @Transactional
    fun removeReaction(postId: Long, memberUid: UUID, type: ReactionType) {
        val member = memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND)

        val memberId = requireNotNull(member.id)

        val reaction = postReactionRepository.findByPostIdAndMemberIdAndType(postId, memberId, type)
            ?: throwError(ErrorType.REACTION_NOT_FOUND, "postId=$postId", "type=$type")

        postReactionRepository.delete(reaction)

        postRepository.decrementReactionCount(postId)
        postRepository.updateScore(postId)
    }
}
