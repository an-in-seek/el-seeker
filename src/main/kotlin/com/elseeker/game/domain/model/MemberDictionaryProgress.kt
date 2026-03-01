package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import com.elseeker.study.domain.model.Dictionary
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "member_dictionary_progress",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_dictionary_progress",
            columnNames = ["member_id", "dictionary_id"]
        )
    ],
    indexes = [
        Index(name = "IDX_member_dictionary_progress_member", columnList = "member_id")
    ]
)
class MemberDictionaryProgress(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictionary_id", nullable = false)
    val dictionary: Dictionary,

    @Column(name = "total_solved_count", nullable = false)
    var totalSolvedCount: Int = 0,

    @Column(name = "word_level", nullable = false)
    var wordLevel: Int = 1,

    @Column(name = "last_solved_at")
    var lastSolvedAt: Instant? = null

) : BaseTimeEntity(id = id) {

    fun incrementSolved() {
        this.totalSolvedCount++
        this.wordLevel = calculateLevel(totalSolvedCount)
        this.lastSolvedAt = Instant.now()
    }

    companion object {
        fun calculateLevel(solvedCount: Int): Int = when {
            solvedCount >= 10 -> 4
            solvedCount >= 6 -> 3
            solvedCount >= 3 -> 2
            else -> 1
        }
    }
}
