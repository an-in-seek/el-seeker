package com.elseeker.bible.domain.model

import com.elseeker.bible.domain.vo.BibleTranslationType
import com.elseeker.bible.domain.vo.LanguageCode
import jakarta.persistence.*

@Entity
@Table(name = "bible_translation")
class BibleTranslation(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    val translationType: BibleTranslationType, // 번역본을 Enum으로 저장

    @Column(nullable = false, unique = true)
    val name: String, // 번역본 이름 (예: 개역개정, NIV)

    @Column(nullable = false)
    val translationOrder: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    val languageCode: LanguageCode,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "translationId")
    val books: MutableList<BibleBook> = mutableListOf()
)
