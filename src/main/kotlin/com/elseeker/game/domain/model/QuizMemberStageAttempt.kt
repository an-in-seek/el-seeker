package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.game.domain.vo.QuizStageAttemptMode
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "quiz_member_stage_attempt",
    indexes = [
        Index(name = "IDX_quiz_stage_attempt_member", columnList = "member_id"),
        Index(name = "IDX_quiz_stage_attempt_stage", columnList = "stage_number")
    ]
)
class QuizMemberStageAttempt(

    id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "stage_number", nullable = false)
    val stageNumber: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    val mode: QuizStageAttemptMode,

    @Column(name = "score", nullable = false)
    var score: Int,

    @Column(name = "question_count", nullable = false)
    var questionCount: Int,

    @Column(name = "started_at", nullable = false)
    val startedAt: Instant,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stageAttempt", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("id ASC")
    private val _questionAttempts: MutableList<QuizMemberQuestionAttempt> = mutableListOf()
) : BaseTimeEntity(
    id = id,
) {

    // 외부 노출용 (불변)
    val questionAttempts: List<QuizMemberQuestionAttempt>
        get() = _questionAttempts.toList()

    fun addQuestionAttempt(attempt: QuizMemberQuestionAttempt) {
        _questionAttempts.add(attempt)
    }

    // 비즈니스 로직: 게임 완료
    fun complete(score: Int, questionCount: Int, completedAt: Instant?) {
        this.score = score
        this.questionCount = questionCount
        this.completedAt = completedAt ?: Instant.now()
    }
}
