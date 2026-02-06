package com.elseeker.community.adapter.input.api.response

import com.elseeker.community.domain.vo.CommentStatus
import com.elseeker.community.domain.vo.PostStatus
import com.elseeker.community.domain.vo.PostType
import com.neovisionaries.i18n.CountryCode
import com.neovisionaries.i18n.LanguageCode
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "게시글 요약 응답")
data class PostSummaryResponse(
    @field:Schema(description = "게시글 ID", example = "1")
    val id: Long,
    @field:Schema(description = "게시글 유형 (NOTICE / FREE / QUESTION / PRAY)", example = "FREE")
    val type: PostType,
    @field:Schema(description = "제목", example = "오늘의 묵상")
    val title: String,
    @field:Schema(description = "본문 미리보기", example = "오늘 말씀을 읽다가 마음에 와닿는 구절이 있어서 공유합니다.")
    val preview: String,
    @field:Schema(description = "작성자 닉네임", example = "은혜")
    val authorNickname: String,
    @field:Schema(description = "게시글 상태", example = "PUBLISHED")
    val status: PostStatus,
    @field:Schema(description = "조회수", example = "42")
    val viewCount: Long,
    @field:Schema(description = "반응 수", example = "5")
    val reactionCount: Long,
    @field:Schema(description = "댓글 수", example = "3")
    val commentCount: Long,
    @field:Schema(description = "인기글 여부", example = "false")
    val isPopular: Boolean,
    @field:Schema(description = "작성일시 (UTC)", example = "2025-01-15T10:30:00Z")
    val createdAt: Instant,
)

@Schema(description = "게시글 상세 응답")
data class PostDetailResponse(
    @field:Schema(description = "게시글 ID", example = "1")
    val id: Long,
    @field:Schema(description = "게시글 유형 (NOTICE / FREE / QUESTION / PRAY)", example = "FREE")
    val type: PostType,
    @field:Schema(description = "언어 코드", example = "ko")
    val language: LanguageCode,
    @field:Schema(description = "국가 코드", example = "KR")
    val country: CountryCode,
    @field:Schema(description = "제목")
    val title: String,
    @field:Schema(description = "본문 내용")
    val content: String,
    @field:Schema(description = "작성자 닉네임")
    val authorNickname: String,
    @field:Schema(description = "작성자 프로필 이미지 URL")
    val authorProfileImageUrl: String?,
    @field:Schema(description = "게시글 상태")
    val status: PostStatus,
    @field:Schema(description = "조회수")
    val viewCount: Long,
    @field:Schema(description = "반응 수")
    val reactionCount: Long,
    @field:Schema(description = "댓글 수")
    val commentCount: Long,
    @field:Schema(description = "인기 점수")
    val score: Long,
    @field:Schema(description = "인기글 여부")
    val isPopular: Boolean,
    @field:Schema(description = "댓글 사용 여부")
    val useReply: Boolean,
    @field:Schema(description = "HTML 본문 여부")
    val isHtml: Boolean,
    @field:Schema(description = "작성일시 (UTC)")
    val createdAt: Instant,
    @field:Schema(description = "수정일시 (UTC)")
    val updatedAt: Instant,
)

@Schema(description = "게시글 Slice 응답 (클라이언트)")
data class PostSliceResponse(
    @field:Schema(description = "게시글 목록")
    val content: List<PostSummaryResponse>,
    @field:Schema(description = "다음 페이지 존재 여부")
    val hasNext: Boolean,
    @field:Schema(description = "페이지 크기")
    val size: Int,
    @field:Schema(description = "현재 페이지 번호")
    val number: Int,
)

@Schema(description = "게시글 Page 응답 (관리자)")
data class PostPageResponse(
    @field:Schema(description = "게시글 목록")
    val content: List<PostSummaryResponse>,
    @field:Schema(description = "전체 게시글 수")
    val totalElements: Long,
    @field:Schema(description = "전체 페이지 수")
    val totalPages: Int,
    @field:Schema(description = "페이지 크기")
    val size: Int,
    @field:Schema(description = "현재 페이지 번호")
    val number: Int,
)

@Schema(description = "댓글 응답")
data class CommentResponse(
    @field:Schema(description = "댓글 ID", example = "1")
    val id: Long,
    @field:Schema(description = "댓글 내용")
    val content: String,
    @field:Schema(description = "작성자 닉네임")
    val authorNickname: String,
    @field:Schema(description = "작성자 프로필 이미지 URL")
    val authorProfileImageUrl: String?,
    @field:Schema(description = "댓글 상태")
    val status: CommentStatus,
    @field:Schema(description = "작성일시 (UTC)")
    val createdAt: Instant,
)

@Schema(description = "댓글 Slice 응답")
data class CommentSliceResponse(
    @field:Schema(description = "댓글 목록")
    val content: List<CommentResponse>,
    @field:Schema(description = "다음 페이지 존재 여부")
    val hasNext: Boolean,
)
