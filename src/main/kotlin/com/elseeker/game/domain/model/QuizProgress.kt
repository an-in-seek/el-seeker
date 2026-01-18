package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*

@Entity
@Table(
    name = "quiz_progress",
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_quiz_progress_member",
            columnNames = ["member_id"]
        )
    ],
    indexes = [
        Index(
            name = "IDX_quiz_progress_member",
            columnList = "member_id"
        )
    ]
)
class QuizProgress(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "current_stage", nullable = false)
    var currentStage: Int = 1,

    @Column(name = "last_completed_stage", nullable = false)
    var lastCompletedStage: Int = 0
) : BaseTimeEntity()
