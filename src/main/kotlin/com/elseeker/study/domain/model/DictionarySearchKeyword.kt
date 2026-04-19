package com.elseeker.study.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * WARNING: 모든 쓰기는 DictionarySearchKeywordRepository.upsertCount(...) 네이티브 UPSERT 로만 수행한다.
 * JpaRepository 의 save() / merge() 를 통한 쓰기는 BaseTimeEntity 의 @LastModifiedDate 와 충돌할 수 있으므로 사용 금지.
 */
@Entity
@Table(
    name = "dictionary_search_keyword",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_dictionary_search_keyword_normalized",
            columnNames = ["normalized_keyword"]
        )
    ],
    indexes = [
        Index(
            name = "idx_dictionary_search_keyword_count_desc",
            columnList = "blocked, search_count DESC, last_searched_at DESC"
        ),
        Index(
            name = "idx_dictionary_search_keyword_last_searched_at",
            columnList = "last_searched_at DESC"
        ),
    ]
)
class DictionarySearchKeyword(

    id: Long? = null,

    @Column(name = "normalized_keyword", nullable = false, length = 50)
    val normalizedKeyword: String,

    @Column(name = "keyword", nullable = false, length = 50)
    var keyword: String,

    @Column(name = "search_count", nullable = false)
    var searchCount: Long = 0,

    @Column(name = "blocked", nullable = false)
    var blocked: Boolean = false,

    @Column(name = "last_searched_at", nullable = false)
    var lastSearchedAt: Instant,

) : BaseTimeEntity(id = id)
