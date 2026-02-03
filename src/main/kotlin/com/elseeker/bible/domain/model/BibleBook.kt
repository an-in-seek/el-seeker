package com.elseeker.bible.domain.model

import com.elseeker.bible.adapter.output.jpa.BibleBookKeyConverter
import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.bible.domain.vo.BibleTestamentType
import com.elseeker.common.domain.BaseEntity
import jakarta.persistence.*

/**
 * 성경 책 (창세기, 마태복음 등)
 */
@Entity
@Table(
    name = "bible_book",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_translation_book_order",
            columnNames = ["translation_id", "book_order"]
        ),
        UniqueConstraint(
            name = "uk_translation_book_key",
            columnNames = ["translation_id", "book_key"]
        )
    ]
)
class BibleBook(

    id: Long? = null,

    @JoinColumn(name = "translation_id", nullable = false)
    val translationId: Long,

    @Convert(converter = BibleBookKeyConverter::class)
    @Column(name = "book_key", nullable = false, length = 4)
    val bookKey: BibleBookKey,

    @Column(name = "book_order", nullable = false)
    val bookOrder: Int,

    @Column(nullable = false)
    val name: String, // 책 이름 (예: 창세기)

    @Column(nullable = false)
    val abbreviation: String, // 약어 (예: 창, 마)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val testamentType: BibleTestamentType, // 구약/신약 구분 (예: OLD, NEW)

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bookId")
    val chapters: MutableList<BibleChapter> = mutableListOf()
) : BaseEntity(
    id = id,
)
