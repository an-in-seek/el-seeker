package com.elseeker.study.domain.model

import jakarta.persistence.*
import java.time.Instant

/**
 * 성경 사전
 */
@Entity
@Table(
    name = "dictionary",
    indexes = [
        Index(name = "IDX_dictionary_term", columnList = "term")
    ]
)
class Dictionary(

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
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {

    @PreUpdate
    fun updateTimestamp() {
        updatedAt = Instant.now()
    }
}
