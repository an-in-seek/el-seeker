package com.elseeker.game.domain.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant

/**
 * 성경 타이핑 절(Verse)을 표현하는 도메인 엔티티
 *
 * 역할:
 * - 하나의 성경 절에 대한 타이핑 진행 상태와 결과를 관리한다.
 * - 세션(BibleTypingSession)에 종속된 하위 엔티티이다.
 *
 * 도메인 책임:
 * - 사용자가 입력한 텍스트와 소요 시간만을 사실(Fact)로 받아들인다.
 * - 정확도(accuracy)와 타자 속도(CPM)는 엔티티 내부 로직을 통해 계산한다.
 *
 * 설계 원칙:
 * - 정확도와 CPM은 파생 값(Derived Value)이며 Source of Truth가 아니다.
 * - 외부 계층(Service/Controller)은 계산 결과를 주입하지 않는다.
 * - 모든 계산 규칙은 도메인 모델 내부에 캡슐화한다.
 *
 * JPA 매핑 특징:
 * - (session_id + verse_number) 복합 PK를 사용한다.
 * - session_id는 @MapsId를 통해 연관관계에서 자동 주입된다.
 *
 * 변경 정책:
 * - 절의 상태 변경은 updateProgress() 메서드를 통해서만 허용한다.
 * - 필드 직접 변경은 허용하지 않는다.
 */
@Entity
@Table(name = "bible_typing_verse")
@EntityListeners(AuditingEntityListener::class)
class BibleTypingVerse(

    /**
     * 복합 PK (session_id + verse_number)
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
     * 절 완료 여부
     */
    @Column(nullable = false)
    var completed: Boolean = false,

    /**
     * 정확도 (0 ~ 100)
     *
     * - 파생 값
     * - (정확 문자 수 / 입력 문자 수) × 100
     */
    @Column(nullable = false)
    var accuracy: Double = 0.0,

    /**
     * 타자 속도 (CPM)
     *
     * - (입력 문자 수 / 경과 시간) × 60
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
     * 절 번호를 엔티티 필드처럼 사용하기 위한 편의 프로퍼티
     */
    @get:Transient
    val verseNumber: Int
        get() = id.verseNumber

    /**
     * 절 진행 상태 갱신 (도메인 단일 진입점)
     *
     * @param typedText 사용자가 입력한 텍스트
     * @param elapsedSeconds 누적 소요 시간(초)
     * @param completed 절 완료 여부
     */
    fun updateProgress(
        typedText: String,
        elapsedSeconds: Int,
        completed: Boolean
    ) {
        require(elapsedSeconds >= 0) { "elapsedSeconds must be >= 0" }

        this.typedText = typedText
        this.elapsedSeconds = elapsedSeconds
        this.completed = completed

        recalcAccuracy()
        recalcCpm()
    }

    // ---------- Private calculation logic ----------

    /**
     * 입력된 텍스트를 기준으로 정확도를 재계산한다.
     *
     * 정확도는 다음 공식으로 계산된다.
     *
     * ```
     * 정확도(%) = (정확히 입력한 문자 수 / 전체 입력 문자 수) × 100
     * ```
     *
     * 계산 규칙:
     * - 공백을 포함한 문자 단위로 비교한다.
     * - 원문과 입력 텍스트를 인덱스 기준으로 비교한다.
     * - 입력 텍스트가 비어 있으면 정확도는 0으로 설정된다.
     *
     * ⚠️ 정확도는 파생 값이며, 별도의 상태로서 신뢰하지 않는다.
     * Source of Truth는 `originalText`와 `typedText`이다.
     */
    private fun recalcAccuracy() {
        if (typedText.isEmpty()) {
            this.accuracy = 0.0
            return
        }
        val correctChars = countCorrectChars(
            originalText = originalText,
            typedText = typedText
        )
        this.accuracy = (correctChars.toDouble() / typedText.length) * 100.0
    }

    /**
     * 입력된 텍스트와 경과 시간을 기준으로 CPM(Character Per Minute)을 재계산한다.
     *
     * CPM은 다음 공식으로 계산된다.
     *
     * ```
     * CPM = (전체 입력 문자 수 / 전체 경과 시간(초)) × 60
     * ```
     *
     * 계산 규칙:
     * - 공백을 포함한 입력 문자 수를 기준으로 한다.
     * - 경과 시간이 0 이하인 경우 계산할 수 없으므로 CPM은 0으로 설정된다.
     *
     * ⚠️ CPM은 파생 값이며 Source of Truth가 아니다.
     * 실제 기준 데이터는 `typedText.length`와 `elapsedSeconds`이다.
     */
    private fun recalcCpm() {
        if (elapsedSeconds <= 0) {
            this.cpm = 0.0
            return
        }
        this.cpm = (typedText.length.toDouble() / elapsedSeconds) * 60.0
    }

    /**
     * 원문과 입력 텍스트를 비교하여 정확히 입력한 문자 수를 계산한다.
     *
     * 비교 규칙:
     * - 공백을 포함한 문자 단위로 비교한다.
     * - 문자열의 index(위치)를 기준으로 1:1 비교한다.
     * - 두 문자열 중 더 짧은 길이를 기준으로 비교를 수행한다.
     *
     * 예시:
     * ```
     * originalText = "ABCDEF"
     * typedText    = "ABXDEF"
     * result       = 5
     * ```
     *
     * @param originalText 기준이 되는 원문 텍스트
     * @param typedText 사용자가 입력한 텍스트
     * @return 정확히 일치하는 문자 개수
     */
    private fun countCorrectChars(
        originalText: String,
        typedText: String
    ): Int {
        val limit = minOf(originalText.length, typedText.length)
        var correct = 0
        for (i in 0 until limit) {
            if (originalText[i] == typedText[i]) {
                correct++
            }
        }
        return correct
    }

    // ---------- Factory ----------

    companion object {

        /**
         * BibleTypingVerse 생성 팩토리 메서드
         *
         * 역할:
         * - 특정 세션에 속한 절(Verse) 엔티티를 생성한다.
         * - 복합 PK(session_id + verse_number) 중 verse_number만 명시적으로 설정한다.
         *
         * JPA 매핑 주의사항:
         * - session_id는 직접 설정하지 않는다.
         * - @MapsId에 의해 session 엔티티가 영속화(flush)되는 시점에
         *   session.id 값이 자동으로 BibleTypingVerseId.sessionId에 주입된다.
         *
         * 설계 의도:
         * - 엔티티 생성 시점에 session의 PK 존재 여부에 의존하지 않는다.
         * - JPA 식별자 생성 전략과 영속성 컨텍스트 규칙을 도메인 로직에서 분리한다.
         *
         * @param session 절이 속한 타이핑 세션
         * @param verseNumber 성경 절 번호
         * @param originalText 해당 절의 원문 텍스트
         * @return 생성된 BibleTypingVerse 엔티티
         */
        fun create(
            session: BibleTypingSession,
            verseNumber: Int,
            originalText: String
        ) = BibleTypingVerse(
            id = BibleTypingVerseId(
                verseNumber = verseNumber
            ),
            session = session,
            originalText = originalText
        )
    }
}

/**
 * BibleTypingVerse의 복합 식별자(Value Object)
 *
 * 구성:
 * - sessionId  : 타이핑 세션 식별자
 * - verseNumber: 성경 절 번호
 *
 * 설계 의도:
 * - 하나의 세션 내에서 절 번호는 유일해야 한다.
 * - (session_id + verse_number) 조합으로 BibleTypingVerse를 식별한다.
 *
 * JPA 매핑 특징:
 * - sessionId는 @MapsId를 통해 BibleTypingVerse.session 연관관계에서 주입된다.
 * - 엔티티 생성 시 sessionId를 직접 설정하지 않아도 된다.
 * - 실제 값은 영속성 컨텍스트 flush 시점에 자동으로 동기화된다.
 *
 * ⚠️ 주의:
 * - 이 클래스는 식별자(Value Object) 용도로만 사용한다.
 * - 비즈니스 로직이나 상태 변경 책임을 두지 않는다.
 */
@Embeddable
data class BibleTypingVerseId(

    /**
     * 세션 ID
     *
     * - BibleTypingVerse.session 연관관계에 의해 관리된다.
     * - @MapsId를 통해 자동 주입되므로 nullable 로 선언된다.
     */
    @Column(name = "session_id")
    val sessionId: Long? = null,

    /**
     * 절 번호
     *
     * - 성경 장 내 절의 순서를 의미한다.
     * - 세션 범위 내에서 유일하다.
     */
    @Column(name = "verse_number")
    val verseNumber: Int

) : Serializable {

    companion object {

        /**
         * BibleTypingVerseId 생성 헬퍼 메서드
         *
         * 사용 목적:
         * - 테스트 코드나 조회 조건 구성 시
         * - sessionId가 이미 확정된 상황에서 명시적으로 ID를 생성할 때 사용한다.
         *
         * @param sessionId 세션 식별자
         * @param verseNumber 절 번호
         * @return BibleTypingVerseId 인스턴스
         */
        fun of(
            sessionId: Long,
            verseNumber: Int
        ) = BibleTypingVerseId(
            sessionId = sessionId,
            verseNumber = verseNumber
        )
    }

}
