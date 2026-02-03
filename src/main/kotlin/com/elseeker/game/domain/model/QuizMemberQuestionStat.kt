package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import kotlin.math.roundToInt

@Entity
@Table(
    name = "quiz_member_question_stat",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_quiz_question_stat_member_question",
            columnNames = ["member_id", "question_id"]
        )
    ],
    indexes = [
        Index(
            name = "IDX_quiz_question_stat_question",
            columnList = "question_id"
        )
    ]
)
class QuizMemberQuestionStat(

    id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: QuizQuestion,

    @Column(nullable = false)
    var attempts: Int = 0,

    @Column(nullable = false)
    var correct: Int = 0
) : BaseTimeEntity(
    id = id,
) {

    companion object {
        fun accuracyPercent(attempts: Long, correct: Long): Int? {
            if (attempts <= 0) return null
            return ((correct.toDouble() / attempts.toDouble()) * 100).roundToInt()
        }
    }

}
