package com.elseeker.community.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.community.adapter.input.api.client.response.CommentResponse
import com.elseeker.community.adapter.input.api.client.response.CommentSliceResponse
import com.elseeker.community.adapter.output.jpa.CommentReportRepository
import com.elseeker.community.adapter.output.jpa.CommentRepository
import com.elseeker.community.adapter.output.jpa.PostRepository
import com.elseeker.community.application.mapper.toResponse
import com.elseeker.community.domain.model.Comment
import com.elseeker.community.domain.model.CommentReport
import com.elseeker.community.domain.vo.CommentStatus
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.ReportReason
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.vo.MemberRole
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val commentReportRepository: CommentReportRepository,
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository,
) {

    @Transactional(readOnly = true)
    fun getComments(postId: Long, pageable: Pageable): CommentSliceResponse {
        val post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        if (post.status == PostStatus.HIDDEN) throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        val slice = commentRepository.findByPostIdWithAuthor(postId, CommentStatus.PUBLISHED, pageable)
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

    @Transactional
    fun updateComment(commentId: Long, memberUid: UUID, content: String): CommentResponse {
        val comment = commentRepository.findByIdWithAuthor(commentId) ?: throwError(ErrorType.COMMENT_NOT_FOUND, "commentId=$commentId")
        if (comment.status == CommentStatus.DELETED) throwError(ErrorType.COMMENT_NOT_FOUND, "commentId=$commentId")
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        if (comment.author.id != member.id && member.memberRole != MemberRole.ADMIN) {
            throwError(ErrorType.COMMENT_ACCESS_DENIED, "commentId=$commentId")
        }
        comment.update(content = content)
        return comment.toResponse()
    }

    @Transactional
    fun deleteComment(commentId: Long, memberUid: UUID) {
        val comment = commentRepository.findByIdWithAuthor(commentId) ?: throwError(ErrorType.COMMENT_NOT_FOUND, "commentId=$commentId")
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        if (comment.author.id != member.id && member.memberRole != MemberRole.ADMIN) {
            throwError(ErrorType.COMMENT_ACCESS_DENIED, "commentId=$commentId")
        }

        val postId = comment.post.id ?: throwError(ErrorType.POST_NOT_FOUND, "commentId=$commentId")

        val deletedFromPublished = commentRepository.updateStatusIfMatch(
            commentId = commentId,
            expectedStatus = CommentStatus.PUBLISHED,
            newStatus = CommentStatus.DELETED,
        )

        if (deletedFromPublished > 0) {
            postRepository.decrementCommentCount(postId)
            postRepository.updateScore(postId)
            return
        }

        val deletedFromHidden = commentRepository.updateStatusIfMatch(
            commentId = commentId,
            expectedStatus = CommentStatus.HIDDEN,
            newStatus = CommentStatus.DELETED,
        )

        if (deletedFromHidden == 0) throwError(ErrorType.COMMENT_NOT_FOUND, "commentId=$commentId")
    }

    @Transactional
    fun restoreComment(commentId: Long, memberUid: UUID) {
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        if (member.memberRole != MemberRole.ADMIN) throwError(ErrorType.ADMIN_ACCESS_DENIED)

        val comment = commentRepository.findById(commentId).orElse(null) ?: throwError(ErrorType.COMMENT_NOT_FOUND, "commentId=$commentId")
        val postId = comment.post.id ?: throwError(ErrorType.POST_NOT_FOUND, "commentId=$commentId")

        val updated = commentRepository.restoreIfIn(
            commentId = commentId,
            fromStatuses = listOf(CommentStatus.HIDDEN, CommentStatus.DELETED),
            publishedStatus = CommentStatus.PUBLISHED,
        )

        if (updated == 0) throwError(ErrorType.COMMENT_NOT_FOUND, "commentId=$commentId")

        postRepository.incrementCommentCount(postId)
        postRepository.updateScore(postId)
    }

    @Transactional
    fun reportComment(commentId: Long, memberUid: UUID, reason: ReportReason) {
        val comment = commentRepository.findById(commentId).orElse(null) ?: throwError(ErrorType.COMMENT_NOT_FOUND, "commentId=$commentId")
        if (comment.status == CommentStatus.DELETED) throwError(ErrorType.COMMENT_NOT_FOUND, "commentId=$commentId")
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        val memberId = requireNotNull(member.id)

        if (commentReportRepository.existsByCommentIdAndReporterId(commentId, memberId)) {
            throwError(ErrorType.REPORT_ALREADY_EXISTS, "commentId=$commentId")
        }

        val report = CommentReport.create(
            comment = comment,
            reporter = member,
            reason = reason,
        )
        try {
            commentReportRepository.save(report)
        } catch (ex: DataIntegrityViolationException) {
            throwError(ErrorType.REPORT_ALREADY_EXISTS, "commentId=$commentId")
        }

        commentRepository.incrementReportCount(commentId)

        val hidden = commentRepository.hideIfReported(
            commentId = commentId,
            threshold = 3,
            publishedStatus = CommentStatus.PUBLISHED,
            hiddenStatus = CommentStatus.HIDDEN,
        )

        if (hidden > 0) {
            val postId = comment.post.id ?: throwError(ErrorType.POST_NOT_FOUND, "commentId=$commentId")
            postRepository.decrementCommentCount(postId)
            postRepository.updateScore(postId)
        }
    }

}
