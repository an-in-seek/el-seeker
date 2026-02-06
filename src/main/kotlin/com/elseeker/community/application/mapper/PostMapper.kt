package com.elseeker.community.application.mapper

import com.elseeker.community.adapter.input.api.response.PostDetailResponse
import com.elseeker.community.adapter.input.api.response.PostSummaryResponse
import com.elseeker.community.domain.model.Post

fun Post.toSummaryResponse() = PostSummaryResponse(
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

fun Post.toDetailResponse() = PostDetailResponse(
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