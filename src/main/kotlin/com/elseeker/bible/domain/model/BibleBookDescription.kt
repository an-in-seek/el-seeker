package com.elseeker.bible.domain.model

import com.elseeker.bible.domain.vo.BibleBookKey
import com.elseeker.bible.domain.vo.LanguageCode
import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(
    name = "bible_book_description",
    indexes = [
        Index(
            name = "IDX_book_description_key_language",
            columnList = "book_key, language_code"
        )
    ]
)
@IdClass(BibleBookDescriptionId::class)
class BibleBookDescription(

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "book_key", nullable = false, length = 32)
    val bookKey: BibleBookKey,

    @Id
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
)

data class BibleBookDescriptionId(
    var bookKey: BibleBookKey = BibleBookKey.GEN,
    var languageCode: LanguageCode = LanguageCode.ko
) : Serializable
