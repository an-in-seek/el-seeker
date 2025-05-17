package com.elseeker.bible.domain.bible.model

import jakarta.persistence.*

@Entity
@Table(name = "bible_book_description")
class BibleBookDescription(

    @Id
    @Column(name = "book_id")
    val bookId: Long,  // BibleBook ID와 동일하게 설정

    @Lob
    @Column(nullable = false)
    val content: String,

    @MapsId  // PK = FK 구조
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    val book: BibleBook
)
