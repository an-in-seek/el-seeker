package com.elseeker.bible.domain.model

import com.elseeker.common.domain.BaseEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "bible_reading_progress",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_bible_reading_progress_member_chapter",
            columnNames = ["member_id", "translation_id", "book_order", "chapter_number"]
        )
    ]
)
class BibleReadingProgress(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "translation_id", nullable = false)
    val translationId: Long,

    @Column(name = "book_order", nullable = false)
    val bookOrder: Int,

    @Column(name = "chapter_number", nullable = false)
    val chapterNumber: Int,

    @Column(name = "read_at", nullable = false)
    val readAt: Instant = Instant.now(),
) : BaseEntity(id = id)
