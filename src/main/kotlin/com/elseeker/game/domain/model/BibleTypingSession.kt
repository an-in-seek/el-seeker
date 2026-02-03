package com.elseeker.game.domain.model

import com.elseeker.common.domain.BaseTimeEntity
import com.elseeker.member.domain.model.Member
import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * 성경 타이핑 세션 도메인 엔티티
 *
 * 하나의 세션은 다음 범위로 유일하게 식별된다.
 * - 회원(member)
 * - 번역본(translationId)
 * - 책(bookOrder)
 * - 장(chapterNumber)
 *
 * 설계 원칙
 * - 정확도(accuracy)와 CPM은 절 평균이 아닌 "누적 값 기반"으로 계산한다.
 * - totalTypedChars / totalCorrectChars / totalElapsedSeconds가 통계 계산의 Source of Truth 역할을 한다.
 * - accuracy, cpm은 조회 성능을 위한 파생 값(캐시)이다.
 */
@Entity
@Table(
    name = "bible_typing_session",
    uniqueConstraints = [
        /**
         * 외부 공개용 세션 식별자 - URL, 공유, API 노출용
         */
        UniqueConstraint(
            name = "uk_bible_typing_session_uid",
            columnNames = ["session_key"]
        ),

        /**
         * 동일 사용자의 동일 범위 세션 단일화 - 한 사용자는 동일한 (번역본 + 책 + 장)에 대해 동시에 하나의 세션만 가질 수 있다.
         */
        UniqueConstraint(
            name = "uk_bible_typing_session_scope",
            columnNames = [
                "member_id",
                "translation_id",
                "book_order",
                "chapter_number"
            ]
        )
    ]
)
class BibleTypingSession(

    id: Long? = null,
    
    /** 외부 노출용 세션 키 */
    @Column(nullable = false, unique = true)
    val sessionKey: UUID = UUID.randomUUID(),

    /** 세션 소유자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    /** 번역본 ID */
    @Column(nullable = false)
    val translationId: Long,

    /** 책 순서 (창세기=1, 출애굽기=2 …) */
    @Column(nullable = false)
    val bookOrder: Int,

    /** 장 번호 */
    @Column(nullable = false)
    val chapterNumber: Int,

    /** 목표 구절 수 (해당 장의 전체 절 수) */
    @Column(nullable = false)
    var totalVerses: Int,

    /**
     * 전체 입력 문자 수 (공백 포함)
     *
     * - CPM 계산의 기준 값
     * - 정확도 계산의 분모
     */
    @Column(nullable = false)
    var totalTypedChars: Int = 0,

    /**
     * 정확히 입력한 문자 수
     *
     * - 정확도 계산의 분자
     * - accuracy는 이 값을 기반으로 재계산 가능
     */
    @Column(nullable = false)
    var totalCorrectChars: Int = 0,

    /** 완료한 구절 수 */
    @Column(nullable = false)
    var completedVerses: Int = 0,

    /**
     * 세션 정확도 (%)
     *
     * 파생 값이며 Source of Truth가 아니다.
     * - totalCorrectChars / totalTypedChars 로 언제든 재계산 가능
     */
    @Column(nullable = false)
    var accuracy: Double = 0.0,

    /**
     * 세션 CPM (Characters Per Minute)
     *
     * 계산식:
     * (전체 입력 문자 수 / 전체 경과 시간(초)) × 60
     *
     * 절별 CPM 평균 방식은 사용하지 않는다.
     */
    @Column(nullable = false)
    var cpm: Double = 0.0,

    /** 전체 경과 시간 (초 단위 누적) */
    @Column(nullable = false)
    var totalElapsedSeconds: Int = 0,

    /** 세션 시작 시각 */
    @Column(nullable = false)
    val startedAt: Instant,

    /** 세션 종료 시각 */
    @Column
    var endedAt: Instant? = null,

    /**
     * 세션에 포함된 구절 타이핑 결과
     *
     * - 상세 히스토리 / 리플레이 / 회고 분석 용도
     */
    @OneToMany(
        mappedBy = "session",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val verses: MutableList<BibleTypingVerse> = mutableListOf()

) : BaseTimeEntity(
    id = id,
) {

    /**
     * 구절 타이핑 결과를 세션 상태에 반영한다.
     *
     * 이 메서드는 세션 상태 변경의 **유일한 진입점**이다.
     * 외부에서는 직접 필드를 수정하지 않고 반드시 이 메서드를 통해 반영해야 한다.
     *
     * @param previousElapsedSeconds 이전 누적 경과 시간(초)
     * @param currentElapsedSeconds 현재 누적 경과 시간(초)
     * @param typedChars 해당 구절에서 입력한 문자 수 (공백 포함)
     * @param verseAccuracy 해당 구절 정확도 (%)
     * @param completed 구절 완료 여부
     */
    fun applyVerseResult(
        previousElapsedSeconds: Int,
        currentElapsedSeconds: Int,
        typedChars: Int,
        verseAccuracy: Double,
        completed: Boolean
    ) {
        adjustElapsedSeconds(previousElapsedSeconds, currentElapsedSeconds)

        if (completed && typedChars > 0) {
            updateAccuracy(typedChars, verseAccuracy)
            this.completedVerses += 1
            recalcCpm()
        }
    }

    /**
     * 세션을 종료 상태로 전환한다.
     *
     * 단순히 종료 시각을 설정하는 역할이지만,
     * 추후 종료 조건 검증이나 도메인 이벤트 발행을 고려해
     * setter 대신 명시적인 메서드로 유지한다.
     *
     * @param endedAt 세션 종료 시각
     */
    fun end(endedAt: Instant) {
        this.endedAt = endedAt
    }

    // ---------------- Private Methods ----------------

    /**
     * 경과 시간을 누적 방식으로 갱신한다.
     *
     * 이전 누적 값과 현재 누적 값의 차이를 더하는 방식이며,
     * 음수 값이 누적되지 않도록 방어 로직을 포함한다.
     *
     * @param previous 이전 누적 경과 시간(초)
     * @param current 현재 누적 경과 시간(초)
     */
    private fun adjustElapsedSeconds(previous: Int, current: Int) {
        val diff = current - previous
        this.totalElapsedSeconds = (this.totalElapsedSeconds + diff).coerceAtLeast(0)
    }

    /**
     * 정확도를 누적 문자 기준으로 갱신한다.
     *
     * 정확도 계산식: (정확히 입력한 문자 수 / 전체 입력 문자 수) × 100
     *
     * 절 단위 평균이 아닌 문자 가중 평균 방식이다.
     *
     * @param typedChars 해당 구절에서 입력한 문자 수
     * @param verseAccuracy 해당 구절 정확도 (%)
     */
    private fun updateAccuracy(
        typedChars: Int,
        verseAccuracy: Double
    ) {
        // 해당 구절에서 정확히 입력한 문자 수를 계산한다.
        // verseAccuracy(%)를 typedChars에 적용해 "정답 문자 수"로 환산
        // 예: typedChars=50, verseAccuracy=90 → correctChars=45
        val correctChars = ((typedChars * verseAccuracy) / 100.0).toInt()

        // 세션 전체 기준으로 입력한 문자 수를 누적한다.
        // (정확도와 CPM 계산의 분모 역할)
        this.totalTypedChars += typedChars

        // 세션 전체 기준으로 정확히 입력한 문자 수를 누적한다.
        // (정확도 계산의 분자 역할)
        this.totalCorrectChars += correctChars

        // 누적된 문자 기준으로 세션 정확도를 재계산한다.
        // 정확도 = (전체 정확 문자 수 / 전체 입력 문자 수) × 100
        // totalTypedChars가 0인 경우는 방어적으로 0.0 처리
        this.accuracy =
            if (this.totalTypedChars == 0) 0.0
            else (this.totalCorrectChars.toDouble() / this.totalTypedChars) * 100.0
    }

    /**
     * CPM(Characters Per Minute)을 누적 값 기준으로 재계산한다.
     *
     * CPM 계산식: (전체 입력 문자 수 / 전체 경과 시간(초)) × 60
     */
    private fun recalcCpm() {
        if (this.totalElapsedSeconds <= 0) {
            this.cpm = 0.0
            return
        }
        this.cpm = (this.totalTypedChars.toDouble() / this.totalElapsedSeconds) * 60.0
    }

    // ---------------- Factory ----------------

    companion object {

        /**
         * BibleTypingSession 생성 팩토리 메서드.
         *
         * 세션은 이 메서드를 통해서만 생성되며,
         * 외부에서 생성자를 직접 호출하지 않는다.
         *
         * @param member 세션 소유자
         * @param translationId 번역본 ID
         * @param bookOrder 책 순서
         * @param chapterNumber 장 번호
         * @param totalVerses 해당 장의 전체 구절 수
         * @return 생성된 BibleTypingSession 인스턴스
         */
        fun create(
            member: Member,
            translationId: Long,
            bookOrder: Int,
            chapterNumber: Int,
            totalVerses: Int
        ): BibleTypingSession {
            return BibleTypingSession(
                sessionKey = UUID.randomUUID(),
                member = member,
                translationId = translationId,
                bookOrder = bookOrder,
                chapterNumber = chapterNumber,
                totalVerses = totalVerses,
                startedAt = Instant.now()
            )
        }
    }

}
