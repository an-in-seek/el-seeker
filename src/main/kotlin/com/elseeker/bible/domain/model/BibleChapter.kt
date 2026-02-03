package com.elseeker.bible.domain.model

import com.elseeker.common.domain.BaseEntity
import jakarta.persistence.*

/**
 * 성경 장 (1장, 2장 등)
 */
@Entity
@Table(
    name = "bible_chapter",
    indexes = [
        Index(
            name = "IDX_chapter_book_number",
            columnList = "book_id, chapter_number"
        )
    ]
)
class BibleChapter(

    id: Long? = null,

    @JoinColumn(name = "book_id", nullable = false)
    val bookId: Long,

    @Column(nullable = false)
    val chapterNumber: Int,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "chapterId")
    val verses: MutableList<BibleVerse> = mutableListOf()
) : BaseEntity(
    id = id,
) {

    companion object {
        fun of(
            bookId: Long,
            chapterNumber: Int
        ) = BibleChapter(
            bookId = bookId,
            chapterNumber = chapterNumber,
        )
    }
}
