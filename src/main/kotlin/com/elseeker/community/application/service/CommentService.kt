package com.elseeker.community.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.community.adapter.input.api.response.CommentResponse
import com.elseeker.community.adapter.input.api.response.CommentSliceResponse
import com.elseeker.community.adapter.output.jpa.CommentRepository
import com.elseeker.community.adapter.output.jpa.PostRepository
import com.elseeker.community.application.mapper.toResponse
import com.elseeker.community.domain.model.Comment
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.member.adapter.output.jpa.MemberRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository,
) {

    @Transactional(readOnly = true)
    fun getComments(postId: Long, pageable: Pageable): CommentSliceResponse {
        val post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        if (post.status == PostStatus.HIDDEN) throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        val slice = commentRepository.findByPostIdWithAuthor(postId, pageable)
        return CommentSliceResponse(
            content = slice.toList().map(Comment::toResponse),
            hasNext = slice.hasNext(),
        )
    }

    @Transactional
    fun createComment(postId: Long, memberUid: UUID, content: String): CommentResponse {
        val post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        if (post.status == PostStatus.HIDDEN) throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        if (!post.useReply) throwError(ErrorType.COMMENT_DISABLED, "postId=$postId")
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        val comment = Comment.create(
            post = post,
            author = member,
            content = content,
        )
        val saved = commentRepository.save(comment)
        postRepository.incrementCommentCount(postId)
        postRepository.updateScore(postId)
        return saved.toResponse()
    }

}
