package com.elseeker.community.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.community.adapter.input.api.client.request.CreatePostRequest
import com.elseeker.community.adapter.input.api.client.request.UpdatePostRequest
import com.elseeker.community.adapter.input.api.client.response.PostDetailResponse
import com.elseeker.community.adapter.input.api.client.response.PostPageResponse
import com.elseeker.community.adapter.input.api.client.response.PostSliceResponse
import com.elseeker.community.adapter.input.api.client.response.PostSummaryResponse
import com.elseeker.community.adapter.output.jpa.PostKotlinJDSL
import com.elseeker.community.adapter.output.jpa.PostReactionRepository
import com.elseeker.community.adapter.output.jpa.PostReportRepository
import com.elseeker.community.adapter.output.jpa.PostRepository
import com.elseeker.community.application.mapper.toDetailResponse
import com.elseeker.community.application.mapper.toSummaryResponse
import com.elseeker.community.domain.model.Post
import com.elseeker.community.domain.model.PostReport
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import com.elseeker.community.domain.vo.ReactionType
import com.elseeker.community.domain.vo.ReportReason
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.vo.MemberRole
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class PostService(
    private val postRepository: PostRepository,
    private val postReportRepository: PostReportRepository,
    private val postReactionRepository: PostReactionRepository,
    private val memberRepository: MemberRepository,
) {

    @Transactional(readOnly = true)
    fun getClientPosts(
        type: PostType?,
        sort: String,
        pageable: Pageable,
    ): PostSliceResponse {
        val normalizedPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize)
        val slice = postRepository.findSlice(normalizedPageable) { PostKotlinJDSL.of(type, sort) }
        return PostSliceResponse(
            content = slice.filterNotNull().map(Post::toSummaryResponse),
            hasNext = slice.hasNext(),
            size = slice.size,
            number = slice.number,
        )
    }

    @Transactional(readOnly = true)
    fun getAdminPosts(
        type: PostType?,
        status: PostStatus?,
        pageable: Pageable,
    ): PostPageResponse {
        val normalizedPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize)
        val page = postRepository.findPage(normalizedPageable) { PostKotlinJDSL.of(type, status) }
        return PostPageResponse(
            content = page.filterNotNull().map(Post::toSummaryResponse),
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            size = page.size,
            number = page.number,
        )
    }

    @Transactional
    fun getPostDetail(postId: Long, memberUid: UUID? = null): PostDetailResponse {
        postRepository.incrementViewCount(postId)
        postRepository.updateScore(postId)
        val post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        if (post.status == PostStatus.HIDDEN) throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        val isLiked = memberUid?.let { uid ->
            val memberId = memberRepository.findIdByUid(uid) ?: return@let false
            postReactionRepository.existsByPostIdAndMemberIdAndType(postId, memberId, ReactionType.LIKE)
        } ?: false
        return post.toDetailResponse(memberUid, isLiked)
    }

    @Transactional(readOnly = true)
    fun getAdminPostDetail(postId: Long): PostDetailResponse {
        val post = postRepository.findByIdWithAuthor(postId) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        return post.toDetailResponse()
    }

    @Transactional
    fun createPost(memberUid: UUID, request: CreatePostRequest): PostDetailResponse {
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        val post = Post.create(
            author = member,
            postType = request.type,
            language = request.language,
            country = request.country,
            title = request.title,
            content = request.content,
            useReply = request.useReply,
            isHtml = request.isHtml,
            isWrittenByAdmin = member.memberRole == MemberRole.ADMIN,
        )
        val saved = postRepository.save(post)
        return saved.toDetailResponse(memberUid)
    }

    @Transactional
    fun updatePost(postId: Long, memberUid: UUID, request: UpdatePostRequest): PostDetailResponse {
        val post = postRepository.findByIdWithAuthor(postId) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        if (post.author.id != member.id && member.memberRole != MemberRole.ADMIN) throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        post.update(title = request.title, content = request.content)
        return post.toDetailResponse(memberUid)
    }

    @Transactional
    fun deletePost(postId: Long, memberUid: UUID) {
        val post = postRepository.findByIdWithAuthor(postId) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        if (post.author.id != member.id && member.memberRole != MemberRole.ADMIN) throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        post.delete()
    }

    @Transactional
    fun hidePost(postId: Long, memberUid: UUID) {
        val post = postRepository.findByIdWithAuthor(postId) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        if (member.memberRole != MemberRole.ADMIN) throwError(ErrorType.ADMIN_ACCESS_DENIED)
        post.hide()
    }

    @Transactional(readOnly = true)
    fun getTopPosts(): List<PostSummaryResponse> {
        val sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS)
        return postRepository.findSlice(PageRequest.of(0, 3)) {
            PostKotlinJDSL.from(sevenDaysAgo)
        }.filterNotNull().map(Post::toSummaryResponse)
    }

    @Transactional
    fun reportPost(postId: Long, memberUid: UUID, reason: ReportReason) {
        val post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        val memberId = requireNotNull(member.id)

        if (postReportRepository.existsByPostIdAndReporterId(postId, memberId)) {
            throwError(ErrorType.REPORT_ALREADY_EXISTS, "postId=$postId")
        }

        val report = PostReport.create(
            post = post,
            reporter = member,
            reason = reason,
        )
        try {
            postReportRepository.save(report)
        } catch (ex: DataIntegrityViolationException) {
            throwError(ErrorType.REPORT_ALREADY_EXISTS, "postId=$postId")
        }

        postRepository.incrementReportCount(postId)
        postRepository.hideIfReported(
            postId = postId,
            threshold = 3,
            publishedStatus = PostStatus.PUBLISHED,
            hiddenStatus = PostStatus.HIDDEN,
        )
    }
}
