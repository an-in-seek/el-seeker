package com.elseeker.bible.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * WARNING: 모든 쓰기는 BibleSearchKeywordRepository.upsertCount(...) 네이티브 UPSERT 로만 수행한다.
 * JpaRepository 가 노출하는 save() / merge() 로 쓰기 시 BaseTimeEntity 의 @LastModifiedDate 가
 * 동작하여 UPSERT 로 셋팅한 updated_at 을 덮어쓸 수 있으므로 사용 금지.
 */
@Entity
@Table(
    name = "bible_search_keyword",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_bible_search_keyword_normalized",
            columnNames = ["normalized_keyword"]
        )
    ],
    indexes = [
        Index(
            name = "idx_bible_search_keyword_count_desc",
            columnList = "search_count DESC, last_searched_at DESC"
        ),
        Index(
            name = "idx_bible_search_keyword_last_searched_at",
            columnList = "last_searched_at DESC"
        ),
    ]
)
class BibleSearchKeyword(

    id: Long? = null,

    @Column(name = "normalized_keyword", nullable = false, length = 50)
    val normalizedKeyword: String,

    @Column(name = "keyword", nullable = false, length = 50)
    var keyword: String,

    @Column(name = "search_count", nullable = false)
    var searchCount: Long = 0,

    @Column(name = "last_searched_at", nullable = false)
    var lastSearchedAt: Instant,

) : BaseTimeEntity(id = id)
