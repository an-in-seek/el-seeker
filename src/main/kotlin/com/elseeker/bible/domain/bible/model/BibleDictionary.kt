package com.elseeker.bible.domain.bible.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 성경 사전 용어
 */
@Entity
@Table(
    name = "bible_dictionary",
    indexes = [
        Index(name = "IDX_dictionary_term", columnList = "term")
    ]
)
class BibleDictionary(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val term: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "related_verses", columnDefinition = "TEXT")
    val relatedVerses: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    @PreUpdate
    fun updateTimestamp() {
        updatedAt = LocalDateTime.now()
    }
}
