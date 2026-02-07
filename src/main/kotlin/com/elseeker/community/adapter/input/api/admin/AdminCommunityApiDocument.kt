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
import com.elseeker.community.domain.vo.CommentStatus
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import com.elseeker.community.domain.vo.TargetType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
        @Parameter(description = "검색 키워드 (제목/본문)") keyword: String?,
        @Parameter(description = "작성자 닉네임") author: String?,
        pageable: Pageable,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<PostPageResponse>

    @Operation(summary = "게시글 상세 조회 (관리자)", description = "게시글 상세 정보를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "게시글 없음"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun getAdminPostDetail(
        @Parameter(description = "게시글 ID") postId: Long,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<PostDetailResponse>

    @Operation(summary = "게시글 생성 (관리자)", description = "게시글을 생성합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "생성 성공"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun createPost(
        @Valid request: CreatePostRequest,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<PostDetailResponse>

    @Operation(summary = "게시글 수정 (관리자)", description = "게시글을 수정합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "404", description = "게시글 없음"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun updatePost(
        @Parameter(description = "게시글 ID") postId: Long,
        @Valid request: UpdatePostRequest,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<PostDetailResponse>

    @Operation(summary = "게시글 삭제 (관리자)", description = "게시글을 삭제합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "404", description = "게시글 없음"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun deletePost(
        @Parameter(description = "게시글 ID") postId: Long,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<Void>

    @Operation(summary = "게시글 상태 변경 (관리자)", description = "게시글 상태를 변경합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "변경 성공"),
        ApiResponse(responseCode = "404", description = "게시글 없음"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun updatePostStatus(
        @Parameter(description = "게시글 ID") postId: Long,
        @Valid request: AdminPostStatusRequest,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<Void>

    @Operation(summary = "댓글 목록 조회 (관리자)", description = "댓글 목록을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun getAdminComments(
        @Parameter(description = "댓글 상태 필터") status: CommentStatus?,
        @Parameter(description = "게시글 ID 필터") postId: Long?,
        @Parameter(description = "댓글 ID 필터") commentId: Long?,
        @Parameter(description = "검색 키워드 (내용)") keyword: String?,
        @Parameter(description = "작성자 닉네임") author: String?,
        pageable: Pageable,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<AdminPageResponse<AdminCommentItem>>

    @Operation(summary = "댓글 상태 변경 (관리자)", description = "댓글 상태를 변경합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "변경 성공"),
        ApiResponse(responseCode = "404", description = "댓글 없음"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun updateCommentStatus(
        @Parameter(description = "댓글 ID") commentId: Long,
        @Valid request: AdminCommentStatusRequest,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<Void>

    @Operation(summary = "댓글 삭제 (관리자)", description = "댓글을 삭제합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "404", description = "댓글 없음"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun deleteComment(
        @Parameter(description = "댓글 ID") commentId: Long,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<Void>

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

    @Operation(summary = "신고 목록 조회 (관리자)", description = "게시글/댓글 신고 목록을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
    )
    fun getAdminReports(
        @Parameter(description = "대상 유형 필터") targetType: TargetType?,
        pageable: Pageable,
        @Parameter(hidden = true) principal: JwtPrincipal,
    ): ResponseEntity<AdminPageResponse<AdminReportItem>>
}
