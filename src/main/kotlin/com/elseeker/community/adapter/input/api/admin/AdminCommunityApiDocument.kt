package com.elseeker.community.adapter.input.api.admin

import com.elseeker.common.security.jwt.JwtPrincipal
import com.elseeker.community.adapter.input.api.client.response.PostPageResponse
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity

@Tag(name = "Community Admin", description = "커뮤니티 관리자 API")
interface AdminCommunityApiDocument {

    @Operation(summary = "게시글 목록 조회 (관리자)", description = "Page 기반 게시글 목록을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun getAdminPosts(
        @Parameter(description = "게시글 유형 필터") type: PostType?,
        @Parameter(description = "게시글 상태 필터") status: PostStatus?,
        pageable: Pageable,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<PostPageResponse>

    @Operation(summary = "댓글 복구 (관리자)", description = "숨김/삭제된 댓글을 복구합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "복구 성공"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
        ApiResponse(responseCode = "404", description = "댓글 없음"),
    )
    fun restoreComment(
        @Parameter(description = "댓글 ID") commentId: Long,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<Void>
}
