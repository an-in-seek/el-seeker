package com.elseeker.community.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.community.adapter.input.api.request.CreatePostRequest
import com.elseeker.community.adapter.input.api.request.UpdatePostRequest
import com.elseeker.community.adapter.input.api.response.PostDetailResponse
import com.elseeker.community.adapter.input.api.response.PostPageResponse
import com.elseeker.community.adapter.input.api.response.PostSliceResponse
import com.elseeker.community.adapter.input.api.response.PostSummaryResponse
import com.elseeker.community.adapter.output.jpa.PostKotlinJDSL
import com.elseeker.community.adapter.output.jpa.PostRepository
import com.elseeker.community.application.mapper.toDetailResponse
import com.elseeker.community.application.mapper.toSummaryResponse
import com.elseeker.community.domain.model.Post
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import com.elseeker.member.adapter.output.jpa.MemberRepository
import com.elseeker.member.domain.vo.MemberRole
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
    private val memberRepository: MemberRepository,
) {

    @Transactional(readOnly = true)
    fun getClientPosts(
        type: PostType?,
        sort: String,
        pageable: Pageable,
    ): PostSliceResponse {
        val slice = postRepository.findSlice(pageable) { PostKotlinJDSL.of(type, sort) }
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
        val page = postRepository.findPage(pageable) { PostKotlinJDSL.of(type, status) }
        return PostPageResponse(
            content = page.filterNotNull().map(Post::toSummaryResponse),
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            size = page.size,
            number = page.number,
        )
    }

    @Transactional
    fun getPostDetail(postId: Long): PostDetailResponse {
        postRepository.incrementViewCount(postId)
        postRepository.updateScore(postId)
        val post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        if (post.status == PostStatus.HIDDEN) throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        return post.toDetailResponse()
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
        return saved.toDetailResponse()
    }

    @Transactional
    fun updatePost(postId: Long, memberUid: UUID, request: UpdatePostRequest): PostDetailResponse {
        val post = postRepository.findByIdWithAuthor(postId) ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")
        val member = memberRepository.findByUid(memberUid) ?: throwError(ErrorType.MEMBER_NOT_FOUND)
        if (post.author.id != member.id && member.memberRole != MemberRole.ADMIN) throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        post.update(title = request.title, content = request.content)
        return post.toDetailResponse()
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
}
