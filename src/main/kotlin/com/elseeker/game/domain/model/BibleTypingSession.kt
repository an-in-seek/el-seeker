package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "bible_typing_session")
class BibleTypingSession(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false)
    val translationId: Long,

    @Column(nullable = false)
    val bookOrder: Int,

    @Column(nullable = false)
    val chapterNumber: Int,

    @Column(nullable = false, length = 64)
    val sessionKey: String,

    @Column(nullable = false)
    val totalVerses: Int,

    @Column(nullable = false)
    val completedVerses: Int,

    @Column(nullable = false)
    val totalTypedChars: Int,

    @Column(nullable = false)
    val accuracy: Double,

    @Column(nullable = false)
    val cpm: Double,

    @Column(nullable = false)
    val startedAt: Instant,

    @Column(nullable = false)
    val endedAt: Instant,

    @OneToMany(
        mappedBy = "session",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val verseResults: MutableList<BibleTypingVerseResult> = mutableListOf()

) : BaseTimeEntity() {

    fun addVerseResults(results: List<BibleTypingVerseResult>) {
        verseResults.addAll(results)
    }
}
