package com.elseeker.community.application.mapper

import com.elseeker.community.adapter.input.api.admin.response.AdminCommentItem
import com.elseeker.community.adapter.input.api.client.response.CommentResponse
import com.elseeker.community.domain.model.Comment
import java.util.*

fun Comment.toResponse(memberUid: UUID? = null) = CommentResponse(
    id = requireNotNull(this.id),
    content = this.content,
    authorNickname = this.author.nickname,
    authorProfileImageUrl = this.author.profileImageUrl,
    status = this.status,
    isAuthor = memberUid != null && this.author.uid == memberUid,
    createdAt = this.createdAt,
)

fun Comment.toAdminItem() = AdminCommentItem(
    id = requireNotNull(this.id),
    postId = requireNotNull(this.post.id),
    postTitle = this.post.title,
    content = this.content,
    authorNickname = this.author.nickname,
    status = this.status,
    reportCount = this.reportCount,
    createdAt = this.createdAt,
)
