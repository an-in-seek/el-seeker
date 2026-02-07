package com.elseeker.community.domain.policy

import com.elseeker.community.domain.vo.CommentStatus

object CommentReportPolicy {
    private const val DEFAULT_THRESHOLD = 3L

    fun shouldHide(currentStatus: CommentStatus, reportCount: Long, threshold: Long = DEFAULT_THRESHOLD): Boolean {
        return currentStatus == CommentStatus.PUBLISHED && reportCount >= threshold
    }
}
