//package com.elseeker.game.domain.model
//
//import com.elseeker.common.domain.BaseTimeEntity
//import jakarta.persistence.Column
//import jakarta.persistence.Entity
//import jakarta.persistence.FetchType
//import jakarta.persistence.JoinColumn
//import jakarta.persistence.ManyToOne
//import jakarta.persistence.Table
//
//@Entity
//@Table(name = "bible_typing_verse_result")
//class BibleTypingVerseResult(
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "session_id", nullable = false)
//    val session: BibleTypingSession,
//
//    @Column(nullable = false)
//    val verseNumber: Int,
//
//    @Column(nullable = false, columnDefinition = "TEXT")
//    val originalText: String,
//
//    @Column(nullable = false, columnDefinition = "TEXT")
//    val typedText: String,
//
//    @Column(nullable = false)
//    val accuracy: Double,
//
//    @Column(nullable = false)
//    val completed: Boolean
//) : BaseTimeEntity()
