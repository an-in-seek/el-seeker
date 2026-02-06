package com.elseeker.community.application.mapper

import com.elseeker.community.adapter.input.api.response.CommentResponse
import com.elseeker.community.domain.model.Comment

fun Comment.toResponse() = CommentResponse(
    id = requireNotNull(this.id),
    content = this.content,
    authorNickname = this.author.nickname,
    authorProfileImageUrl = this.author.profileImageUrl,
    status = this.status,
    createdAt = this.createdAt,
)