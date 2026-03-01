package com.elseeker.study.domain.model

import com.elseeker.common.domain.BaseEntity
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "dictionary_reference",
    indexes = [
        Index(name = "IDX_dictionary_reference_dictionary", columnList = "dictionary_id")
    ]
)
class DictionaryReference(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictionary_id", nullable = false)
    val dictionary: Dictionary,

    @Column(name = "verse_reference", nullable = false, length = 100)
    val verseReference: String,

    @Column(name = "verse_excerpt", nullable = false, columnDefinition = "TEXT")
    val verseExcerpt: String,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()

) : BaseEntity(id = id)
