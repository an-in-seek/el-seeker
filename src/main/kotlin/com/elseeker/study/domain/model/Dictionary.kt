package com.elseeker.study.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

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

    id: Long? = null,

    @Column(nullable = false)
    val term: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "related_verses", columnDefinition = "TEXT")
    val relatedVerses: String? = null,
) : BaseTimeEntity(
    id = id,
)
