package com.elseeker.community.adapter.output.jpa

import com.elseeker.community.domain.model.Comment
import com.elseeker.community.domain.vo.CommentStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommentRepository : JpaRepository<Comment, Long> {

    @Query(
        """
        SELECT c FROM Comment c
        JOIN FETCH c.author
        WHERE c.post.id = :postId
          AND c.status = :status
        ORDER BY c.createdAt ASC
        """
    )
    fun findByPostIdWithAuthor(
        @Param("postId") postId: Long,
        @Param("status") status: CommentStatus,
        pageable: Pageable,
    ): Slice<Comment>

    @Query(
        """
        SELECT c FROM Comment c
        JOIN FETCH c.author
        WHERE c.id = :id
        """
    )
    fun findByIdWithAuthor(@Param("id") id: Long): Comment?

    @Modifying
    @Query("UPDATE Comment c SET c.reportCount = c.reportCount + 1 WHERE c.id = :commentId")
    fun incrementReportCount(@Param("commentId") commentId: Long): Int

    @Modifying
    @Query(
        """
        UPDATE Comment c
        SET c.status = :hiddenStatus
        WHERE c.id = :commentId
          AND c.status = :publishedStatus
          AND c.reportCount >= :threshold
        """
    )
    fun hideIfReported(
        @Param("commentId") commentId: Long,
        @Param("threshold") threshold: Long,
        @Param("publishedStatus") publishedStatus: CommentStatus,
        @Param("hiddenStatus") hiddenStatus: CommentStatus,
    ): Int

    @Modifying
    @Query(
        """
        UPDATE Comment c
        SET c.status = :newStatus
        WHERE c.id = :commentId
          AND c.status = :expectedStatus
        """
    )
    fun updateStatusIfMatch(
        @Param("commentId") commentId: Long,
        @Param("expectedStatus") expectedStatus: CommentStatus,
        @Param("newStatus") newStatus: CommentStatus,
    ): Int

    @Modifying
    @Query(
        """
        UPDATE Comment c
        SET c.status = :publishedStatus
        WHERE c.id = :commentId
          AND c.status IN :fromStatuses
        """
    )
    fun restoreIfIn(
        @Param("commentId") commentId: Long,
        @Param("fromStatuses") fromStatuses: Collection<CommentStatus>,
        @Param("publishedStatus") publishedStatus: CommentStatus,
    ): Int
}
