package com.elseeker.community.domain.vo

enum class CommentStatus {
    PUBLISHED, HIDDEN, DELETED;

    fun isCountedInPost(): Boolean = this == PUBLISHED
}
