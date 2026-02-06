package com.elseeker.community.adapter.input.api.admin

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.community.adapter.input.api.client.response.PostPageResponse
import com.elseeker.community.application.service.CommentService
import com.elseeker.community.application.service.PostService
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/v1/admin/community")
class AdminCommunityApi(
    private val postService: PostService,
    private val commentService: CommentService,
) : AdminCommunityApiDocument {

    @GetMapping("/posts")
    override fun getAdminPosts(
        @RequestParam(required = false) type: PostType?,
        @RequestParam(required = false) status: PostStatus?,
        @PageableDefault(size = 20) pageable: Pageable,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<PostPageResponse> {
        val response = postService.getAdminPosts(type, status, pageable)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/comments/{commentId}/restore")
    override fun restoreComment(
        @PathVariable commentId: Long,
        @AuthenticationPrincipal principal: JwtPrincipal,
    ): ResponseEntity<Void> {
        commentService.restoreComment(commentId, principal.memberUid)
        return ResponseEntity.ok().build()
    }
}
