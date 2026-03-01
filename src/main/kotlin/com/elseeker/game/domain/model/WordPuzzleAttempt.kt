package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.game.domain.vo.AttemptStatus
import com.elseeker.game.domain.vo.QuizDifficulty
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "word_puzzle_attempt",
    indexes = [
        Index(name = "IDX_word_puzzle_attempt_member", columnList = "member_id"),
        Index(name = "IDX_word_puzzle_attempt_puzzle", columnList = "word_puzzle_id"),
        Index(name = "IDX_word_puzzle_attempt_member_puzzle_status", columnList = "member_id, word_puzzle_id, attempt_status_code")
    ]
)
class WordPuzzleAttempt(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_puzzle_id", nullable = false)
    val wordPuzzle: WordPuzzle,

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_status_code", nullable = false, length = 20)
    var attemptStatusCode: AttemptStatus = AttemptStatus.IN_PROGRESS,

    @Column(name = "score")
    var score: Int? = null,

    @Column(name = "wrong_submission_count", nullable = false)
    var wrongSubmissionCount: Int = 0,

    @Column(name = "hint_usage_count", nullable = false)
    var hintUsageCount: Int = 0,

    @Column(name = "elapsed_seconds", nullable = false)
    var elapsedSeconds: Int = 0,

    @Column(name = "started_at", nullable = false)
    val startedAt: Instant = Instant.now(),

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @OneToMany(mappedBy = "attempt", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val cells: MutableList<WordPuzzleAttemptCell> = mutableListOf(),

    @OneToMany(mappedBy = "attempt", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val hintUsages: MutableList<WordPuzzleHintUsage> = mutableListOf()

) : BaseTimeEntity(id = id) {

    fun isCompleted(): Boolean = attemptStatusCode == AttemptStatus.COMPLETED

    fun isInProgress(): Boolean = attemptStatusCode == AttemptStatus.IN_PROGRESS

    fun updateElapsedSeconds(clientElapsedSeconds: Int) {
        this.elapsedSeconds = maxOf(this.elapsedSeconds, clientElapsedSeconds)
    }

    fun incrementHintUsage() {
        this.hintUsageCount++
    }

    fun incrementWrongSubmission() {
        this.wrongSubmissionCount++
    }

    fun complete(score: Int, completedAt: Instant = Instant.now()) {
        this.attemptStatusCode = AttemptStatus.COMPLETED
        this.score = score
        this.completedAt = completedAt
    }

    fun calculateScore(difficulty: QuizDifficulty): Int {
        val baseScore = when (difficulty) {
            QuizDifficulty.EASY -> 500
            QuizDifficulty.NORMAL -> 1000
            QuizDifficulty.HARD -> 1500
        }

        val timeLimitSeconds = when (difficulty) {
            QuizDifficulty.EASY -> 300
            QuizDifficulty.NORMAL -> 600
            QuizDifficulty.HARD -> 1200
        }

        val hintPenalty = hintUsageCount * 50
        val wrongPenalty = wrongSubmissionCount * 100

        val timeBonus = if (elapsedSeconds < timeLimitSeconds) {
            (500.0 * (1.0 - elapsedSeconds.toDouble() / timeLimitSeconds)).toInt()
        } else {
            0
        }

        return maxOf(0, baseScore - hintPenalty - wrongPenalty + timeBonus)
    }
}
