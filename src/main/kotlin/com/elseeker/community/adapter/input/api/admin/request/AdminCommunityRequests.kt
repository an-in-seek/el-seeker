package com.elseeker.community.adapter.input.api.admin.request

import com.elseeker.community.domain.vo.CommentStatus
import com.elseeker.community.domain.vo.PostStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "게시글 상태 변경 요청")
data class AdminPostStatusRequest(
    @field:NotNull(message = "게시글 상태는 필수입니다")
    @field:Schema(description = "게시글 상태", example = "HIDDEN")
    val status: PostStatus,
)

@Schema(description = "댓글 상태 변경 요청")
data class AdminCommentStatusRequest(
    @field:NotNull(message = "댓글 상태는 필수입니다")
    @field:Schema(description = "댓글 상태", example = "HIDDEN")
    val status: CommentStatus,
)
