package com.elseeker.study.domain.model

import com.elseeker.common.domain.BaseEntity
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "dictionary_reference",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_dict_ref_verse",
            columnNames = ["dictionary_id", "book_order", "chapter_number", "verse_number"]
        )
    ],
    indexes = [
        Index(name = "IDX_dictionary_reference_dictionary", columnList = "dictionary_id")
    ]
)
class DictionaryReference(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictionary_id", nullable = false)
    val dictionary: Dictionary,

    @Column(name = "book_order", nullable = false)
    val bookOrder: Int,

    @Column(name = "chapter_number", nullable = false)
    val chapterNumber: Int,

    @Column(name = "verse_number", nullable = false)
    val verseNumber: Int,

    @Column(name = "verse_label", nullable = false, length = 100)
    val verseLabel: String,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()

) : BaseEntity(id = id)
