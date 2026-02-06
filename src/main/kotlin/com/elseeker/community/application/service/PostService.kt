package com.elseeker.community.application.service

import com.elseeker.common.domain.ErrorType
import com.elseeker.common.domain.throwError
import com.elseeker.community.adapter.input.api.request.CreatePostRequest
import com.elseeker.community.adapter.input.api.request.UpdatePostRequest
import com.elseeker.community.adapter.input.api.response.*
import com.elseeker.community.adapter.output.jpa.PostRepository
import com.elseeker.community.domain.model.Post
import com.elseeker.community.domain.vo.PostStatistics
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
        val slice = postRepository.findSlice(pageable) {
            select(
                entity(Post::class)
            ).from(
                entity(Post::class),
                fetchJoin(Post::author),
            ).whereAnd(
                path(Post::status).eq(PostStatus.PUBLISHED),
                type?.let { path(Post::type).eq(it) },
            ).orderBy(
                when (sort) {
                    "popular" -> path(Post::statistics).path(PostStatistics::score).desc()
                    else -> path(Post::createdAt).desc()
                }
            )
        }

        return PostSliceResponse(
            content = slice.content.map { it.toSummaryResponse() },
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
        val page = postRepository.findPage(pageable) {
            select(
                entity(Post::class)
            ).from(
                entity(Post::class),
                fetchJoin(Post::author),
            ).whereAnd(
                type?.let { path(Post::type).eq(it) },
                status?.let { path(Post::status).eq(it) },
            ).orderBy(
                path(Post::createdAt).desc()
            )
        }

        return PostPageResponse(
            content = page.content.map { it.toSummaryResponse() },
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

        val post = postRepository.findByIdAndStatusNot(postId, PostStatus.DELETED)
            ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")

        if (post.status == PostStatus.HIDDEN) {
            throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        }

        return post.toDetailResponse()
    }

    @Transactional(readOnly = true)
    fun getAdminPostDetail(postId: Long): PostDetailResponse {
        val post = postRepository.findByIdWithAuthor(postId)
            ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")

        return post.toDetailResponse()
    }

    @Transactional
    fun createPost(memberUid: UUID, request: CreatePostRequest): PostDetailResponse {
        val member = memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND)

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
        val post = postRepository.findByIdWithAuthor(postId)
            ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")

        val member = memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND)

        if (post.author.id != member.id && member.memberRole != MemberRole.ADMIN) {
            throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        }

        post.update(title = request.title, content = request.content)
        return post.toDetailResponse()
    }

    @Transactional
    fun deletePost(postId: Long, memberUid: UUID) {
        val post = postRepository.findByIdWithAuthor(postId)
            ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")

        val member = memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND)

        if (post.author.id != member.id && member.memberRole != MemberRole.ADMIN) {
            throwError(ErrorType.POST_ACCESS_DENIED, "postId=$postId")
        }

        post.delete()
    }

    @Transactional
    fun hidePost(postId: Long, memberUid: UUID) {
        val post = postRepository.findByIdWithAuthor(postId)
            ?: throwError(ErrorType.POST_NOT_FOUND, "postId=$postId")

        val member = memberRepository.findByUid(memberUid)
            ?: throwError(ErrorType.MEMBER_NOT_FOUND)

        if (member.memberRole != MemberRole.ADMIN) {
            throwError(ErrorType.ADMIN_ACCESS_DENIED)
        }

        post.hide()
    }

    @Transactional(readOnly = true)
    fun getTopPosts(): List<PostSummaryResponse> {
        val sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS)

        val slice = postRepository.findSlice(PageRequest.of(0, 3)) {
            select(
                entity(Post::class)
            ).from(
                entity(Post::class),
                fetchJoin(Post::author),
            ).whereAnd(
                path(Post::status).eq(PostStatus.PUBLISHED),
                path(Post::createdAt).greaterThanOrEqualTo(sevenDaysAgo),
            ).orderBy(
                path(Post::statistics).path(PostStatistics::score).desc()
            )
        }

        return slice.content.map { it.toSummaryResponse() }
    }

    private fun Post.toSummaryResponse() = PostSummaryResponse(
        id = requireNotNull(this.id),
        type = this.type,
        title = this.title,
        authorNickname = this.author.nickname,
        status = this.status,
        viewCount = this.statistics.viewCount,
        reactionCount = this.statistics.reactionCount,
        commentCount = this.statistics.commentCount,
        isPopular = this.isPopular,
        createdAt = this.createdAt,
    )

    private fun Post.toDetailResponse() = PostDetailResponse(
        id = requireNotNull(this.id),
        type = this.type,
        language = this.language,
        country = this.country,
        title = this.title,
        content = this.content,
        authorNickname = this.author.nickname,
        authorProfileImageUrl = this.author.profileImageUrl,
        status = this.status,
        viewCount = this.statistics.viewCount,
        reactionCount = this.statistics.reactionCount,
        commentCount = this.statistics.commentCount,
        score = this.statistics.score,
        isPopular = this.isPopular,
        useReply = this.useReply,
        isHtml = this.isHtml,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )
}
