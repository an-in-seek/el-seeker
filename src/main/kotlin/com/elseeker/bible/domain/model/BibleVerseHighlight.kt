package com.elseeker.bible.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*

/**
 * 사용자별 성경 구절 형광펜
 */
@Entity
@Table(
    name = "bible_verse_highlight",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_bible_verse_highlight_member_verse",
            columnNames = ["member_id", "translation_id", "book_order", "chapter_number", "verse_number"]
        )
    ]
)
class BibleVerseHighlight(

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

    @Column(name = "verse_number", nullable = false)
    val verseNumber: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    var color: BibleHighlightColor
) : BaseTimeEntity(
    id = id,
) {

    fun updateColor(color: BibleHighlightColor) {
        this.color = color
    }
}
