package com.elseeker.game.adapter.input.api.client.response

import com.elseeker.game.domain.model.BibleTypingSession
import java.time.Instant


data class BibleTypingSessionResponse(
    /** 외부 공개용 세션 식별자 (UUID) */
    val sessionKey: String,

    /** 세션 생성 시각 */
    val createdAt: Instant
)

data class BibleTypingSessionSummaryResponse(
    /** 외부 공개용 세션 식별자 (UUID) */
    val sessionKey: String,

    /** 번역본 ID */
    val translationId: Long,

    /** 성경 권 순서 */
    val bookOrder: Int,

    /** 장 번호 */
    val chapterNumber: Int,

    /** 전체 절 수 */
    val totalVerses: Int,

    /** 완료한 절 수 */
    val completedVerses: Int,

    /** 정확도 (0~100) */
    val accuracy: Int,

    /** 타자 속도 (CPM) */
    val cpm: Int,

    /** 전체 소요 시간 (초) */
    val totalElapsedSeconds: Int,

    /** 세션 시작 시각 */
    val startedAt: Instant,

    /** 세션 종료 시각 (진행 중이면 null) */
    val endedAt: Instant?,

    /** 세션 생성 시각 */
    val createdAt: Instant
) {
    companion object {
        fun of(session: BibleTypingSession) = BibleTypingSessionSummaryResponse(
            sessionKey = session.sessionKey.toString(),
            translationId = session.translationId,
            bookOrder = session.bookOrder,
            chapterNumber = session.chapterNumber,
            totalVerses = session.totalVerses,
            completedVerses = session.completedVerses,
            accuracy = session.accuracy.toInt(),
            cpm = session.cpm.toInt(),
            startedAt = session.startedAt,
            endedAt = session.endedAt,
            totalElapsedSeconds = session.totalElapsedSeconds,
            createdAt = session.createdAt,
        )
    }
}