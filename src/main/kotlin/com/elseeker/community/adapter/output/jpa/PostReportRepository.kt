package com.elseeker.community.adapter.output.jpa

import com.elseeker.community.domain.model.PostReport
import org.springframework.data.jpa.repository.JpaRepository

interface PostReportRepository : JpaRepository<PostReport, Long> {

    fun existsByPostIdAndReporterId(postId: Long, reporterId: Long): Boolean
}
