package com.elseeker.community.domain.policy

import com.elseeker.community.domain.vo.PostStatus

object PostReportPolicy {
    private const val DEFAULT_THRESHOLD = 3L

    fun shouldHide(currentStatus: PostStatus, reportCount: Long, threshold: Long = DEFAULT_THRESHOLD): Boolean {
        return currentStatus == PostStatus.PUBLISHED && reportCount >= threshold
    }
}
