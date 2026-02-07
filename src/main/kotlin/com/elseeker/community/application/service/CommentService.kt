package com.elseeker.community.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.community.adapter.input.api.client.response.CommentResponse
import com.elseeker.community.adapter.input.api.client.response.CommentSliceResponse
import com.elseeker.community.adapter.output.jpa.CommentRepository
import com.elseeker.community.adapter.output.jpa.CommunityReportRepository
import com.elseeker.community.adapter.output.jpa.PostRepository
import com.elseeker.community.application.mapper.toResponse
import com.elseeker.community.domain.model.Comment
import com.elseeker.community.domain.model.CommunityReport
import com.elseeker.community.domain.policy.CommentReportPolicy
import com.elseeker.community.domain.vo.CommentStatus
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.ReportReason
import com.elseeker.community.domain.vo.TargetType
import com.elseeker.member.adapter.output.jpa.MemberRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val communityReportRepository: CommunityReportRepository,
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository,
) {

    @Transactional(readOnly = true)
    fun getComments(postId: Long, pageable: Pageable, memberUid: UUID? = null): CommentSliceResponse {
        val post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED) ?: throwError(ErrorType.POST_NOT_FOUND)
        post.ensureReadableForClient()
        val slice = commentRepository.findByPostIdWithAuthor(postId, CommentStatus.PUBLISHED, pageable)
        return CommentSliceResponse(
            content = slice.toList().map { it.toResponse(memberUid) },
            hasNext = slice.hasNext(),
        )
    }

    @Transactional(readOnly = true)
    fun getAdminComments(
        status: CommentStatus?,
        postId: Long?,
        commentId: Long?,
        keyword: String?,
        author: String?,
        pageable: Pageable,
    ): Page<Comment> {
        val normalizedPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize)
        val keywordLike = keyword?.trim()?.takeIf { it.isNotBlank() }?.let { "%$it%" }
        val authorLike = author?.trim()?.takeIf { it.isNotBlank() }?.let { "%$it%" }
        return commentRepository.findAdminPage(
            status = status,
            postId = postId,
            commentId = commentId,
            keyword = keywordLike,
            author = authorLike,
            pageable = normalizedPageable,
        )
    }

    @Transactional
    fun createComment(postId: Long, memberUid: UUID, content: String): CommentResponse {
        val post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED) ?: throwError(ErrorType.POST_NOT_FOUND)
        post.ensureReadableForClient()
        if (!post.useReply) throwError(ErrorType.COMMENT_DISABLED)
        val member = getMemberOrThrow(memberUid)
        val comment = Comment.create(
            post = post,
            author = member,
            content = content,
        )
        val saved = commentRepository.save(comment)
        postRepository.incrementCommentCount(postId)
        postRepository.updateScore(postId)
        return saved.toResponse(memberUid)
    }

    @Transactional
    fun updateComment(commentId: Long, memberUid: UUID, content: String): CommentResponse {
        val comment = commentRepository.findByIdWithAuthor(commentId) ?: throwError(ErrorType.COMMENT_NOT_FOUND)
        if (comment.status == CommentStatus.DELETED) throwError(ErrorType.COMMENT_NOT_FOUND)
        val member = getMemberOrThrow(memberUid)
        comment.updateBy(actor = member, content = content)
        return comment.toResponse(memberUid)
    }

    @Transactional
    fun deleteComment(commentId: Long, memberUid: UUID) {
        val comment = commentRepository.findByIdWithAuthorAndPostForUpdate(commentId) ?: throwError(ErrorType.COMMENT_NOT_FOUND)
        val member = getMemberOrThrow(memberUid)
        val before = comment.status
        comment.deleteBy(member)
        applyCommentCountChange(comment.post.id ?: throwError(ErrorType.POST_NOT_FOUND), before, comment.status)
    }

    @Transactional
    fun restoreComment(commentId: Long, memberUid: UUID) {
        val member = getMemberOrThrow(memberUid)
        val comment = commentRepository.findByIdWithAuthorAndPostForUpdate(commentId) ?: throwError(ErrorType.COMMENT_NOT_FOUND)
        val before = comment.status
        comment.restoreByAdmin(member)
        applyCommentCountChange(comment.post.id ?: throwError(ErrorType.POST_NOT_FOUND), before, comment.status)
    }

    @Transactional
    fun updateCommentStatus(commentId: Long, memberUid: UUID, status: CommentStatus) {
        val member = getMemberOrThrow(memberUid)
        val comment = commentRepository.findByIdWithAuthorAndPostForUpdate(commentId) ?: throwError(ErrorType.COMMENT_NOT_FOUND)
        val before = comment.status
        comment.changeStatusByAdmin(member, status)
        applyCommentCountChange(comment.post.id ?: throwError(ErrorType.POST_NOT_FOUND), before, comment.status)
    }

    @Transactional
    fun reportComment(commentId: Long, memberUid: UUID, reason: ReportReason) {
        val comment = commentRepository.findByIdAndStatusNotForUpdate(commentId, CommentStatus.DELETED) ?: throwError(ErrorType.COMMENT_NOT_FOUND)
        val member = getMemberOrThrow(memberUid)
        val memberId = requireNotNull(member.id)
        if (communityReportRepository.existsByTargetTypeAndTargetIdAndReporterId(TargetType.COMMENT, commentId, memberId)) {
            throwError(ErrorType.REPORT_COMMENT_ALREADY_EXISTS)
        }
        val report = CommunityReport.create(
            targetType = TargetType.COMMENT,
            targetId = commentId,
            reporterId = memberId,
            reason = reason,
        )
        communityReportRepository.save(report)
        val before = comment.status
        comment.registerReport(CommentReportPolicy)
        applyCommentCountChange(comment.post.id ?: throwError(ErrorType.POST_NOT_FOUND), before, comment.status)
    }

    private fun getMemberOrThrow(memberUid: UUID) =
        memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)

    private fun applyCommentCountChange(postId: Long, before: CommentStatus, after: CommentStatus) {
        if (before == after) return
        val beforeCounted = before.isCountedInPost()
        val afterCounted = after.isCountedInPost()
        if (beforeCounted == afterCounted) return
        if (afterCounted) {
            postRepository.incrementCommentCount(postId)
        } else {
            postRepository.decrementCommentCount(postId)
        }
        postRepository.updateScore(postId)
    }
}
