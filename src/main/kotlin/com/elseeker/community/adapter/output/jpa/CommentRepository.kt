package com.elseeker.community.adapter.output.jpa

import com.elseeker.community.domain.model.Comment
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommentRepository : JpaRepository<Comment, Long> {

    @Query(
        """
        SELECT c FROM Comment c
        JOIN FETCH c.author
        WHERE c.post.id = :postId
        ORDER BY c.createdAt ASC
        """
    )
    fun findByPostIdWithAuthor(@Param("postId") postId: Long, pageable: Pageable): Slice<Comment>
}
