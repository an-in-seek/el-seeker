package com.elseeker.community.adapter.input.api.request

import com.elseeker.community.domain.vo.PostType
import com.elseeker.community.domain.vo.ReactionType
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.LanguageCode
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "게시글 작성 요청")
data class CreatePostRequest(
    @field:NotNull(message = "게시글 유형은 필수입니다")
    @field:Schema(description = "게시글 유형 (NOTICE / FREE / QUESTION / PRAY)", example = "FREE")
    val type: PostType,

    @field:NotNull(message = "언어 코드는 필수입니다")
    @field:Schema(description = "언어 코드", example = "ko")
    val language: LanguageCode,

    @field:NotNull(message = "국가 코드는 필수입니다")
    @field:Schema(description = "국가 코드", example = "KR")
    val country: CountryCode,

    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(max = 200, message = "제목은 200자 이내여야 합니다")
    @field:Schema(description = "제목", example = "오늘의 묵상")
    val title: String,

    @field:NotBlank(message = "본문은 필수입니다")
    @field:Schema(description = "본문 내용")
    val content: String,

    @field:Schema(description = "댓글 사용 여부", example = "true")
    val useReply: Boolean = true,

    @field:Schema(description = "HTML 본문 여부", example = "false")
    val isHtml: Boolean = false,
)

@Schema(description = "게시글 수정 요청")
data class UpdatePostRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(max = 200, message = "제목은 200자 이내여야 합니다")
    @field:Schema(description = "제목", example = "수정된 제목")
    val title: String,

    @field:NotBlank(message = "본문은 필수입니다")
    @field:Schema(description = "본문 내용")
    val content: String,
)

@Schema(description = "반응 추가 요청")
data class CreateReactionRequest(
    @field:NotNull(message = "반응 유형은 필수입니다")
    @field:Schema(description = "반응 유형", example = "LIKE")
    val type: ReactionType,
)

@Schema(description = "댓글 작성 요청")
data class CreateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 필수입니다")
    @field:Size(max = 1000, message = "댓글은 1000자 이내여야 합니다")
    @field:Schema(description = "댓글 내용", example = "아멘!")
    val content: String,
)
