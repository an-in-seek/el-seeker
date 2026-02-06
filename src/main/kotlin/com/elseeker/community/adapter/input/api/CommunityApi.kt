package com.elseeker.community.adapter.input.api

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.community.adapter.input.api.request.CreateCommentRequest
import com.elseeker.community.adapter.input.api.request.CreatePostRequest
import com.elseeker.community.adapter.input.api.request.CreateReactionRequest
import com.elseeker.community.adapter.input.api.request.UpdatePostRequest
import com.elseeker.community.adapter.input.api.response.*
import com.elseeker.community.application.service.CommentService
import com.elseeker.community.application.service.PostService
import com.elseeker.community.application.service.ReactionService
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import com.elseeker.community.domain.vo.ReactionType
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/v1/community")
class CommunityApi(
    private val postService: PostService,
    private val reactionService: ReactionService,
    private val commentService: CommentService,
) : CommunityApiDocument {

    @GetMapping("/posts")
    override fun getClientPosts(
        @RequestParam(required = false) type: PostType?,
        @RequestParam(defaultValue = "latest") sort: String,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<PostSliceResponse> {
        val response = postService.getClientPosts(type, sort, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/admin/posts")
    override fun getAdminPosts(
        @RequestParam(required = false) type: PostType?,
        @RequestParam(required = false) status: PostStatus?,
        @PageableDefault(size = 20) pageable: Pageable,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<PostPageResponse> {
        val response = postService.getAdminPosts(type, status, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/posts/{postId}")
    override fun getPostDetail(
        @PathVariable postId: Long,
    ): ResponseEntity<PostDetailResponse> {
        val response = postService.getPostDetail(postId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/posts")
    override fun createPost(
        @Valid @RequestBody request: CreatePostRequest,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<PostDetailResponse> {
        val response = postService.createPost(principal.memberUid, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/posts/{postId}")
    override fun updatePost(
        @PathVariable postId: Long,
        @Valid @RequestBody request: UpdatePostRequest,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<PostDetailResponse> {
        val response = postService.updatePost(postId, principal.memberUid, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/posts/{postId}")
    override fun deletePost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<Void> {
        postService.deletePost(postId, principal.memberUid)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/posts/{postId}/reactions")
    override fun addReaction(
        @PathVariable postId: Long,
        @Valid @RequestBody request: CreateReactionRequest,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<Void> {
        reactionService.addReaction(postId, principal.memberUid, request.type)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/posts/{postId}/reactions/{type}")
    override fun removeReaction(
        @PathVariable postId: Long,
        @PathVariable type: ReactionType,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<Void> {
        reactionService.removeReaction(postId, principal.memberUid, type)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/posts/{postId}/comments")
    override fun getComments(
        @PathVariable postId: Long,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<CommentSliceResponse> {
        val response = commentService.getComments(postId, pageable)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/posts/{postId}/comments")
    override fun createComment(
        @PathVariable postId: Long,
        @Valid @RequestBody request: CreateCommentRequest,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<CommentResponse> {
        val response = commentService.createComment(postId, principal.memberUid, request.content)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/posts/top")
    override fun getTopPosts(): ResponseEntity<List<PostSummaryResponse>> {
        val response = postService.getTopPosts()
        return ResponseEntity.ok(response)
    }
}
