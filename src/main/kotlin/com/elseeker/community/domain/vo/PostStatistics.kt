package com.elseeker.community.domain.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class PostStatistics(
    @Column(name = "view_count", nullable = false)
    var viewCount: Long = 0,

    @Column(name = "reaction_count", nullable = false)
    var reactionCount: Long = 0,

    @Column(name = "comment_count", nullable = false)
    var commentCount: Long = 0,

    @Column(name = "report_count", nullable = false)
    var reportCount: Long = 0,

    @Column(name = "score", nullable = false)
    var score: Long = 0,
) {
    companion object {
        fun create() = PostStatistics(
            viewCount = 0,
            reactionCount = 0,
            commentCount = 0,
            reportCount = 0,
            score = 0
        )
    }

    fun incrementReaction() {
        reactionCount += 1
    }

    fun decrementReaction() {
        if (reactionCount > 0) {
            reactionCount -= 1
        }
    }

    fun recalculateScore() {
        score = viewCount + (reactionCount * 5) + (commentCount * 3)
    }
}
