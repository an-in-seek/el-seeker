package com.elseeker.game.domain.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant

@Entity
@Table(name = "bible_typing_verse")
@EntityListeners(AuditingEntityListener::class)
class BibleTypingVerse(

    /**
     * 복합 PK
     * (session_id + verse_number)
     */
    @EmbeddedId
    val id: BibleTypingVerseId,

    /**
     * 세션 연관관계
     * - 복합 PK의 session_id와 매핑
     */
    @MapsId("sessionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    val session: BibleTypingSession,

    /**
     * 원문 텍스트 (절 기준 고정값)
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    val originalText: String,

    /**
     * 사용자가 입력한 텍스트
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    var typedText: String = "",

    /**
     * 해당 절 완료 여부
     */
    @Column(nullable = false)
    var completed: Boolean = false,

    /**
     * 정확도 (0 ~ 100)
     */
    @Column(nullable = false)
    var accuracy: Double = 0.0,

    /**
     * 타자 속도 (CPM)
     */
    @Column(nullable = false)
    var cpm: Double = 0.0,

    /**
     * 소요 시간 (초)
     */
    @Column(nullable = false)
    var elapsedSeconds: Int = 0,

    /**
     * 생성 시각 (Auditing)
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    /**
     * 수정 시각 (Auditing)
     */
    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
) {

    /**
     * verseNumber를 엔티티 필드처럼 쓰고 싶을 때(도메인/응답용)
     * - 실제 컬럼 매핑은 EmbeddedId(id.verseNumber)가 담당
     */
    @get:Transient
    val verseNumber: Int
        get() = id.verseNumber

    fun updateProgress(
        typedText: String,
        accuracy: Double,
        cpm: Double,
        elapsedSeconds: Int,
        completed: Boolean
    ) {
        this.typedText = typedText
        this.accuracy = accuracy
        this.cpm = cpm
        this.elapsedSeconds = elapsedSeconds
        this.completed = completed
    }

    companion object {
        fun create(
            session: BibleTypingSession,
            verseNumber: Int,
            originalText: String
        ): BibleTypingVerse =
            BibleTypingVerse(
                id = BibleTypingVerseId(
                    sessionId = session.id!!,
                    verseNumber = verseNumber
                ),
                session = session,
                originalText = originalText
            )
    }
}

@Embeddable
data class BibleTypingVerseId(
    @Column(name = "session_id")
    val sessionId: Long = 0L,

    @Column(name = "verse_number")
    val verseNumber: Int = 0
) : Serializable {

    companion object {
        fun of(
            sessionId: Long,
            verseNumber: Int,
        ) = BibleTypingVerseId(
            sessionId = sessionId,
            verseNumber = verseNumber
        )
    }
}
