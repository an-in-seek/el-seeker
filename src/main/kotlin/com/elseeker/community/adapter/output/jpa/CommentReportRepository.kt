package com.elseeker.community.adapter.output.jpa

import com.elseeker.community.domain.model.CommentReport
import org.springframework.data.jpa.repository.JpaRepository

interface CommentReportRepository : JpaRepository<CommentReport, Long> {

    fun existsByCommentIdAndReporterId(commentId: Long, reporterId: Long): Boolean
}
