package com.elseeker.bible.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*

@Entity
@Table(
    name = "bible_book_memo",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_bible_book_memo_member_book",
            columnNames = ["member_id", "translation_id", "book_order"]
        )
    ]
)
class BibleBookMemo(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "translation_id", nullable = false)
    val translationId: Long,

    @Column(name = "book_order", nullable = false)
    val bookOrder: Int,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String
) : BaseTimeEntity(
    id = id,
) {

    fun updateContent(content: String) {
        this.content = content
    }
}
