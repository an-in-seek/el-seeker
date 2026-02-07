package com.elseeker.community.adapter.input.api.admin

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.common.adapter.input.api.admin.response.AdminPageResponse
import com.elseeker.community.adapter.input.api.admin.request.AdminCommentStatusRequest
import com.elseeker.community.adapter.input.api.admin.request.AdminPostStatusRequest
import com.elseeker.community.adapter.input.api.admin.response.AdminCommentItem
import com.elseeker.community.adapter.input.api.admin.response.AdminReportItem
import com.elseeker.community.adapter.input.api.client.request.CreatePostRequest
import com.elseeker.community.adapter.input.api.client.request.UpdatePostRequest
import com.elseeker.community.adapter.input.api.client.response.PostDetailResponse
import com.elseeker.community.adapter.input.api.client.response.PostPageResponse
import com.elseeker.community.application.service.CommentService
import com.elseeker.community.application.service.AdminCommunityReportService
import com.elseeker.community.application.service.PostService
import com.elseeker.community.application.mapper.toAdminItem
import com.elseeker.community.domain.vo.CommentStatus
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import com.elseeker.community.domain.vo.TargetType
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@Validated
@RestController
@RequestMapping("/api/v1/admin/community")
class AdminCommunityApi(
    private val postService: PostService,
    private val commentService: CommentService,
    private val adminCommunityReportService: AdminCommunityReportService,
) : AdminCommunityApiDocument {

    @GetMapping("/posts")
    override fun getAdminPosts(
        @RequestParam(required = false) type: PostType?,
        @RequestParam(required = false) status: PostStatus?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) author: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<PostPageResponse> {
        val response = postService.getAdminPosts(type, status, keyword, author, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/posts/{postId}")
    override fun getAdminPostDetail(
        @PathVariable postId: Long,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<PostDetailResponse> {
        val response = postService.getAdminPostDetail(postId)
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

    @PatchMapping("/posts/{postId}/status")
    override fun updatePostStatus(
        @PathVariable postId: Long,
        @Valid @RequestBody request: AdminPostStatusRequest,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<Void> {
        postService.updatePostStatus(postId, principal.memberUid, request.status)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/comments")
    override fun getAdminComments(
        @RequestParam(required = false) status: CommentStatus?,
        @RequestParam(required = false) postId: Long?,
        @RequestParam(required = false) commentId: Long?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) author: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<AdminPageResponse<AdminCommentItem>> {
        val page = commentService.getAdminComments(status, postId, commentId, keyword, author, pageable)
        val response = AdminPageResponse.from(page) { it.toAdminItem() }
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/comments/{commentId}/status")
    override fun updateCommentStatus(
        @PathVariable commentId: Long,
        @Valid @RequestBody request: AdminCommentStatusRequest,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<Void> {
        commentService.updateCommentStatus(commentId, principal.memberUid, request.status)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/comments/{commentId}")
    override fun deleteComment(
        @PathVariable commentId: Long,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<Void> {
        commentService.deleteComment(commentId, principal.memberUid)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/comments/{commentId}/restore")
    override fun restoreComment(
        @PathVariable commentId: Long,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<Void> {
        commentService.restoreComment(commentId, principal.memberUid)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/reports")
    override fun getAdminReports(
        @RequestParam(required = false) targetType: TargetType?,
        @PageableDefault(size = 20) pageable: Pageable,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<AdminPageResponse<AdminReportItem>> {
        val page = adminCommunityReportService.getAdminReports(targetType, pageable)
        val response = AdminPageResponse.from(page) { it }
        return ResponseEntity.ok(response)
    }
}
