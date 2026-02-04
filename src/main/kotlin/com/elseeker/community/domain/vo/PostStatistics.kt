package com.elseeker.community.domain.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class PostStatistics private constructor(
    @Column(nullable = false)
    var viewCount: Long = 0,

    @Column(nullable = false)
    var likeCount: Long = 0,

    @Column(nullable = false)
    var commentCount: Long = 0,

    @Column(nullable = false)
    var reportCount: Long = 0,

    @Column(nullable = false)
    var score: Long = 0,
) {
    companion object {
        fun create() = PostStatistics(
            viewCount = 0,
            likeCount = 0,
            commentCount = 0,
            reportCount = 0,
            score = 0
        )
    }
}
