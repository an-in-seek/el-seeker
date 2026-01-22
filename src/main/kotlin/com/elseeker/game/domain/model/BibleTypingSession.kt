package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "bible_typing_session",
    uniqueConstraints = [
        // 외부 공개용 세션 식별자
        UniqueConstraint(
            name = "uk_bible_typing_session_uid",
            columnNames = ["session_uid"]
        ),
        // 동일 사용자의 동일 범위 세션 단일화
        UniqueConstraint(
            name = "uk_bible_typing_session_scope",
            columnNames = [
                "member_id",
                "translation_id",
                "book_order",
                "chapter_number"
            ]
        )
    ]
)
class BibleTypingSession(

    @Column(nullable = false, unique = true)
    val sessionKey: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false)
    val translationId: Long,

    @Column(nullable = false)
    val bookOrder: Int,

    @Column(nullable = false)
    val chapterNumber: Int,

    @Column(nullable = false)
    var totalVerses: Int,

    @Column(nullable = false)
    var completedVerses: Int = 0,

    @Column(nullable = false)
    var totalTypedChars: Int = 0,

    @Column(nullable = false)
    var accuracy: Double = 0.0,

    @Column(nullable = false)
    var cpm: Double = 0.0,

    @Column(nullable = false)
    val startedAt: Instant,

    @Column
    var endedAt: Instant? = null,

    @OneToMany(
        mappedBy = "session",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val verses: MutableList<BibleTypingVerse> = mutableListOf()

) : BaseTimeEntity() {

    fun updateStats(
        totalVerses: Int,
        completedVerses: Int,
        totalTypedChars: Int,
        accuracy: Double,
        cpm: Double,
        endedAt: Instant
    ) {
        this.totalVerses = totalVerses
        this.completedVerses = completedVerses
        this.totalTypedChars = totalTypedChars
        this.accuracy = accuracy
        this.cpm = cpm
        this.endedAt = endedAt
    }

    companion object {

        /**
         * 세션 생성 (유일한 생성 진입점)
         */
        fun create(
            member: Member,
            translationId: Long,
            bookOrder: Int,
            chapterNumber: Int,
            totalVerses: Int = 0,
        ): BibleTypingSession {
            return BibleTypingSession(
                member = member,
                translationId = translationId,
                bookOrder = bookOrder,
                chapterNumber = chapterNumber,
                sessionKey = UUID.randomUUID(),
                totalVerses = totalVerses,
                completedVerses = 0,
                totalTypedChars = 0,
                accuracy = 0.0,
                cpm = 0.0,
                startedAt = Instant.now(),
                endedAt = null,
                verses = mutableListOf()
            )
        }
    }

}
