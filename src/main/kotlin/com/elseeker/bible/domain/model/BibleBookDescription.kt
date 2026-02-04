package com.elseeker.bible.domain.model

import com.elseeker.bible.adapter.output.jpa.BibleBookKeyConverter
import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.common.domain.BaseEntity
import com.neovisionaries.i18n.LanguageCode
import jakarta.persistence.*

@Entity
@Table(
    name = "bible_book_description",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_book_description_key_language",
            columnNames = ["book_key", "language_code"],
        )
    ]
)
class BibleBookDescription(

    id: Long? = null,

    @Convert(converter = BibleBookKeyConverter::class)
    @Column(name = "book_key", nullable = false, length = 4)
    val bookKey: BibleBookKey,

    @Enumerated(EnumType.STRING)
    @Column(name = "language_code", nullable = false, length = 4)
    val languageCode: LanguageCode,

    @Column(name = "summary", nullable = false)
    val summary: String,

    @Column(name = "author", nullable = false)
    val author: String,

    @Column(name = "written_year", nullable = false)
    val writtenYear: String,

    @Column(name = "historical_period", nullable = false)
    val historicalPeriod: String,

    @Column(name = "background", nullable = false)
    val background: String,

    @Column(name = "content", nullable = false)
    val content: String
) : BaseEntity(
    id = id,
)


