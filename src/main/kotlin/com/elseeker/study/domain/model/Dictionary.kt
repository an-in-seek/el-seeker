package com.elseeker.study.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.study.domain.vo.OriginalLanguage
import jakarta.persistence.*

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

    @Enumerated(EnumType.STRING)
    @Column(name = "original_language_code", length = 10)
    val originalLanguageCode: OriginalLanguage? = null,

    @Column(name = "original_lexeme", length = 200)
    val originalLexeme: String? = null,

    @Column(name = "bible_usage_count", nullable = false)
    val bibleUsageCount: Int = 0,

    @OneToMany(mappedBy = "dictionary", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    val references: MutableList<DictionaryReference> = mutableListOf()

) : BaseTimeEntity(
    id = id,
)
