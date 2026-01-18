package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "bible_typing_verse_progress")
class BibleTypingVerseProgress(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false, length = 64)
    val sessionKey: String,

    @Column(nullable = false)
    val translationId: Long,

    @Column(nullable = false)
    val bookOrder: Int,

    @Column(nullable = false)
    val chapterNumber: Int,

    @Column(nullable = false)
    val verseNumber: Int,

    @Column(nullable = false, columnDefinition = "TEXT")
    val originalText: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val typedText: String,

    @Column(nullable = false)
    val accuracy: Double,

    @Column(nullable = false)
    val completed: Boolean
) : BaseTimeEntity()
