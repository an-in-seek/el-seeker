# 성경 검색 키워드 집계 · 랭킹 설계 문서

## 구현 상태

설계 완료, 미구현.

대상 화면: `src/main/resources/templates/bible/search.html`
기존 검색 API: `GET /api/v1/bibles/translations/{translationId}/search?keyword=...` (`BibleApi.searchBible`)

## 1. 요구사항 요약

1. 비로그인/로그인 사용자 모두 검색 가능 (기존 API는 `permitAll` 처리됨)
2. 검색 요청마다 키워드별 검색 횟수 집계
3. 키워드별 누적 횟수를 PostgreSQL에 영속화
4. 인기 검색어(랭킹) 조회 API 제공
5. Redis 미사용 (로컬 캐시만 사용)

### 1.1 개인정보 처리 방침

- 집계 대상은 **키워드 자체**이며 "누가 검색했는지" 는 기록하지 않는다.
- `BibleSearchKeyword` 테이블에 `member_id` / `member_uid` / IP 등 사용자 식별 정보 없음.
- 이는 로그인/비로그인 동등 처리 요구사항과 개인정보 최소 수집 원칙에 부합.

## 2. 설계 전략

### 2.1 로깅 지점

기존 검색 API에 **집계 부수 효과(side-effect)** 를 추가한다.

- 발행 위치: **`BibleReader.searchBibleVersesSlice()` 내부** (⚠️ 중요)
  - `BibleReader` 는 클래스 레벨 `@Transactional(readOnly = true)` 가 있음
  - 상위 `BibleService.searchBibleVersesSlice()` 는 `@Transactional` 이 없음
  - 따라서 `BibleService` 에서 발행하면 활성 트랜잭션이 없어 `AFTER_COMMIT` 리스너가 발화하지 않음 (Spring 이 이벤트를 조용히 폐기)
  - 반드시 `BibleReader` 내부 (트랜잭션 경계 내)에서 `ApplicationEventPublisher.publishEvent(...)` 호출
  - read-only 트랜잭션도 `AFTER_COMMIT` 이 정상 발화함
- 리스너: `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Transactional(propagation = REQUIRES_NEW)` **동기 실행**
  - 프로젝트 기존 컨벤션인 `GameRankingService.kt` 와 동일 패턴
  - UPSERT 1회(~1ms)는 동기로 충분하며, `@Async` 스레드 풀·`DiscardOldestPolicy` 등의 복잡도가 불필요
  - 리스너 내부 try-catch 로 집계 실패가 사용자 응답에 영향 주지 않도록 방어

이 구조의 이점:
- 검색 트랜잭션이 성공한 경우에만 집계 (실패 검색은 카운트하지 않음)
- 집계가 실패해도 사용자 응답은 이미 반환된 상태 (AFTER_COMMIT 시점이므로)
- 기존 컨벤션 재사용으로 학습 곡선 최소화
- 향후 확장(Kafka 발행, 실시간 지표 등) 시 리스너만 추가하면 됨

### 2.2 키워드 정규화

집계 일관성을 위해 저장 전 정규화:

- `trim()` → 양끝 공백 제거
- `lowercase()` → 영문 소문자화 (한글은 영향 없음)
- 연속 공백 → 단일 공백
- 2자 미만은 집계 제외 (스팸/오타 방지)
- 50자 초과는 집계 제외

정규화된 값은 `normalized_keyword` 컬럼에 저장, 원본 키워드는 `keyword` 컬럼에 별도 보관.

### 2.3 동시성: UPSERT + 원자 증분

Read-modify-write 대신 PostgreSQL 원자 연산 사용. Spring Data JPA 에서는 **반드시 `@Modifying` + `nativeQuery = true`** 로 선언한다.

```kotlin
interface BibleSearchKeywordRepository : JpaRepository<BibleSearchKeyword, Long> {

    @Modifying
    @Query(
        value = """
            INSERT INTO bible_search_keyword
                (normalized_keyword, keyword, search_count, last_searched_at, created_at, updated_at)
            VALUES
                (:normalized, :original, 1, :now, :now, :now)
            ON CONFLICT (normalized_keyword)
            DO UPDATE SET
                search_count     = bible_search_keyword.search_count + 1,
                keyword          = EXCLUDED.keyword,
                last_searched_at = EXCLUDED.last_searched_at,
                updated_at       = EXCLUDED.updated_at
        """,
        nativeQuery = true
    )
    fun upsertCount(
        @Param("normalized") normalized: String,
        @Param("original") original: String,
        @Param("now") now: Instant,
    ): Int

    @Query(
        """
        SELECT new com.elseeker.bible.application.result.SearchKeywordRankingResult(
            e.keyword, e.searchCount
        )
        FROM BibleSearchKeyword e
        ORDER BY e.searchCount DESC, e.lastSearchedAt DESC
        """
    )
    fun findTopRanking(pageable: Pageable): List<SearchKeywordRankingResult>
}
```

- 단일 DB 라운드트립
- `normalized_keyword` 유니크 제약 기반의 row-level lock 만 획득 → 다른 키워드끼리는 경합 없음
- `SELECT FOR UPDATE` 나 애플리케이션 레벨 락 불필요
- `upsertCount` 호출 시점의 `created_at` 은 INSERT 때만 의미를 갖고, `ON CONFLICT` 분기에서는 update 되지 않는다 (PostgreSQL 기본 동작)

> `keyword` 컬럼은 UPSERT 마다 최신 원본으로 덮어쓴다. 예: "사랑" 검색 후 " 사랑 " 검색 시 `normalized_keyword` 는 같고 `keyword` 만 " 사랑 " 으로 바뀜. 랭킹 화면에는 정규화 전 최신 원본이 표시된다. 표기 일관성을 중시한다면 `keyword` 컬럼을 INSERT 시에만 쓰고 UPSERT 시 유지하는 방향으로 변경 가능(`ON CONFLICT ... SET` 에서 `keyword` 를 제외).

## 3. 도메인 구조

기존 `bible` 모듈 내에 추가.

```
bible/
├─ domain/model/BibleSearchKeyword.kt           -- 엔티티 (BaseTimeEntity 상속)
├─ domain/vo/NormalizedKeyword.kt               -- 정규화 VO
├─ domain/event/BibleSearchPerformedEvent.kt    -- 검색 이벤트 (data class)
├─ application/service/BibleSearchKeywordService.kt
├─ application/listener/BibleSearchKeywordListener.kt  -- @TransactionalEventListener (동기)
├─ application/result/SearchKeywordRankingResult.kt
├─ adapter/input/api/client/BibleSearchKeywordApi.kt
├─ adapter/input/api/client/BibleSearchKeywordApiDocument.kt
├─ adapter/input/api/client/response/SearchKeywordRankingResponse.kt
└─ adapter/output/jpa/BibleSearchKeywordRepository.kt
```

`BibleReader.searchBibleVersesSlice()` 내부(기존 `@Transactional(readOnly = true)` 경계 안)에서 `applicationEventPublisher.publishEvent(BibleSearchPerformedEvent(keyword))` 호출 추가. 이를 위해 `BibleReader` 생성자에 `ApplicationEventPublisher` 를 주입한다.

### 3.1 주요 코드 스켈레톤

**이벤트 (domain/event):**
```kotlin
data class BibleSearchPerformedEvent(val keyword: String)
```

**리스너 (application/listener):**
```kotlin
@Component
class BibleSearchKeywordListener(
    private val bibleSearchKeywordService: BibleSearchKeywordService,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onSearchPerformed(event: BibleSearchPerformedEvent) {
        try {
            bibleSearchKeywordService.increment(event.keyword)
        } catch (e: Exception) {
            logger.warn("Failed to increment search keyword count: {}", e.message)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BibleSearchKeywordListener::class.java)
    }
}
```

- 리스너는 반드시 **별도 `@Component` 빈** 이어야 한다. Spring AOP 프록시가 관여해야 `@Transactional(REQUIRES_NEW)` 가 동작하므로, 발행 측(`BibleReader`) 과 리스너 측(`BibleSearchKeywordListener`) 은 서로 다른 빈으로 분리 필수 (SelfInvocation 회피).
- `AFTER_COMMIT` 이므로 `BibleReader` 의 read-only 트랜잭션이 정상 종료된 뒤에만 실행된다.

**서비스 (application/service):**
```kotlin
@Service
class BibleSearchKeywordService(
    private val repository: BibleSearchKeywordRepository,
) {

    @Transactional
    fun increment(rawKeyword: String) {
        val normalized = NormalizedKeyword.ofOrNull(rawKeyword) ?: return
        repository.upsertCount(normalized.value, rawKeyword.trim(), Instant.now())
    }

    @Cacheable(value = ["bible-search-keyword-ranking"], key = "#limit")
    @Transactional(readOnly = true)
    fun getRanking(limit: Int): List<SearchKeywordRankingResult> {
        if (limit !in 1..50) {
            throwError(ErrorType.INVALID_PARAMETER)
        }
        return repository.findTopRanking(PageRequest.of(0, limit))
    }
}
```

> `@Modifying` 네이티브 쿼리는 활성 트랜잭션이 필수이므로 `increment()` 에 `@Transactional` 필수. 리스너의 `REQUIRES_NEW` 와 자연스럽게 합쳐져 동일 트랜잭션에서 실행된다.

**정규화 VO (domain/vo):**
```kotlin
@JvmInline
value class NormalizedKeyword private constructor(val value: String) {
    companion object {
        fun ofOrNull(raw: String): NormalizedKeyword? {
            val collapsed = raw.trim().replace(WHITESPACE_REGEX, " ").lowercase()
            if (collapsed.length !in 2..50) return null
            return NormalizedKeyword(collapsed)
        }
        private val WHITESPACE_REGEX = Regex("\\s+")
    }
}
```

**랭킹 결과 DTO (application/result):**
```kotlin
data class SearchKeywordRankingResult(
    val keyword: String,
    val searchCount: Long,
)
```

> JPQL `SELECT new ...` 생성자 프로젝션이 참조하는 경로(`com.elseeker.bible.application.result.SearchKeywordRankingResult`) 와 클래스 패키지·필드 순서가 정확히 일치해야 한다.

## 4. DB 테이블 설계

### 4.1 테이블 정의

```sql
CREATE TABLE bible_search_keyword (
    id                  BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    normalized_keyword  VARCHAR(50)  NOT NULL,
    keyword             VARCHAR(50)  NOT NULL,
    search_count        BIGINT       NOT NULL DEFAULT 0,
    last_searched_at    TIMESTAMP(6) NOT NULL,
    created_at          TIMESTAMP(6) NOT NULL,
    updated_at          TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_bible_search_keyword_normalized UNIQUE (normalized_keyword)
);

COMMENT ON TABLE  bible_search_keyword                    IS '성경 검색 키워드 누적 집계';
COMMENT ON COLUMN bible_search_keyword.normalized_keyword IS '정규화 키워드 (집계 키, 유니크)';
COMMENT ON COLUMN bible_search_keyword.keyword            IS '최근 검색 원본 키워드';
COMMENT ON COLUMN bible_search_keyword.search_count       IS '누적 검색 횟수';
COMMENT ON COLUMN bible_search_keyword.last_searched_at   IS '최근 검색 시각 (UTC)';
```

### 4.2 인덱스

```sql
CREATE INDEX idx_bible_search_keyword_count_desc
    ON bible_search_keyword (search_count DESC, last_searched_at DESC);

CREATE INDEX idx_bible_search_keyword_last_searched_at
    ON bible_search_keyword (last_searched_at DESC);
```

- 랭킹 조회는 `ORDER BY search_count DESC LIMIT N` → 복합 인덱스 한 번 탐색으로 완료
- `last_searched_at` 단독 인덱스는 "최근 N일간 인기 검색어" 등의 확장 쿼리와 보관 정책 배치에 사용

### 4.3 컬럼 선택 근거

- `normalized_keyword VARCHAR(50)`: 실무 검색 키워드 길이 한계로 충분. 유니크 인덱스 크기 관리.
- `keyword VARCHAR(50)`: 원본 보존 (대소문자 표기 차이 확인/디버깅용)
- `search_count BIGINT`: 인기 키워드 장기 누적 대비 (INT 한계 21억 초과 가능성 배제)
- FK 없음: 검색 키워드는 독립 집계 데이터

## 5. API 설계

### 5.1 검색 (기존, 동작만 확장)

`GET /api/v1/bibles/translations/{translationId}/search?keyword={q}&bookOrder={n}&page=0&size=20`

- 기존 응답 스펙 유지
- 서비스 내부에서 이벤트 발행 추가 (사용자에게 투명)
- 정규화 실패(2자 미만 등) 키워드는 집계만 건너뛰고 검색은 정상 수행

### 5.2 인기 검색어 조회 (신규)

`GET /api/v1/bibles/search-keywords/ranking?limit=10`

쿼리 파라미터:
- `limit`: 1~50, 기본 10

응답 예시:

```json
{
  "items": [
    { "rank": 1, "keyword": "사랑",  "searchCount": 1523 },
    { "rank": 2, "keyword": "믿음",  "searchCount": 1102 },
    { "rank": 3, "keyword": "은혜",  "searchCount":  987 }
  ],
  "refreshedAt": "2026-04-19T10:15:30Z"
}
```

- 권한: `permitAll` (기존 `/api/v1/bibles/**` 가 이미 `SecurityConfig.kt` 에서 `permitAll` 처리됨 → 별도 설정 불필요)
- `keyword` 는 표시용으로 `normalized_keyword` 가 아닌 `keyword`(가장 최근 원본) 를 반환
- 블랙리스트 처리된 키워드는 응답에서 자동 제외 (§5.4 참조)
- `limit` 범위를 벗어나면 `ServiceError(ErrorType.INVALID_PARAMETER)` 를 던져 `GlobalExceptionHandler` 가 400 JSON 응답 반환

### 5.3 관리자 상세 조회 (Phase 2)

`GET /api/v1/admin/bible/search-keywords?page=0&size=50&sort=searchCount,desc`

- 운영자용 페이지네이션 조회 + 키워드 검색

### 5.4 관리자 블랙리스트 (공개 랭킹 노출 제어)

공개 랭킹에 부적절한 키워드(욕설·광고성·개인정보 등)가 노출되는 것을 방지하기 위해 블랙리스트 기능이 필요.

두 가지 접근 중 택일:
- **간단 접근**: `BibleSearchKeyword` 에 `is_blocked BOOLEAN NOT NULL DEFAULT false` 컬럼 추가, 랭킹 쿼리에서 `WHERE is_blocked = false` 필터
- **분리 접근**: 별도 `bible_search_keyword_blacklist(normalized_keyword UNIQUE)` 테이블

초기 구현은 **간단 접근**으로 충분. 관리자 API:
- `PATCH /api/v1/admin/bible/search-keywords/{id}` → `{ "blocked": true }`

이 엔드포인트는 Phase 2 범위.

## 6. 캐시 전략 (Redis 미사용)

### 6.1 로컬 캐시: Caffeine

- `@Cacheable("bible-search-keyword-ranking")` + Caffeine `expireAfterWrite=30s`, `maximumSize=16`
- 랭킹 쿼리는 대부분 동일한 `limit` 값으로 반복 호출 → 캐시 적중률 높음
- TTL 30초 → 인기 검색어가 "실시간"일 필요는 없음. 운영 체감 충분
- 인스턴스별 개별 캐시지만, 랭킹은 본질적으로 eventually consistent 한 지표이므로 무방

`build.gradle.kts` 추가:
```kotlin
implementation("com.github.ben-manes.caffeine:caffeine")
implementation("org.springframework.boot:spring-boot-starter-cache")
```

프로젝트에는 현재 `@EnableCaching` 이 적용된 구성 클래스가 없으므로 `common/config/CacheConfig.kt` 를 새로 만든다:

```kotlin
@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager = CaffeineCacheManager("bible-search-keyword-ranking").apply {
        setCaffeine(
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(30))
                .maximumSize(16)
        )
    }
}
```

`@EnableCaching` 없이 `@Cacheable` 을 붙이면 아무 동작도 하지 않으므로 반드시 함께 추가한다.

### 6.2 집계 쓰기 측 캐시 (Phase 2)

초기 버전은 UPSERT 직접 호출.
트래픽이 커지면 메모리 버퍼 + 주기 flush 적용 (§8.2 참조).

## 7. 성능 · 동시성 고려

### 7.1 예상 부하 (초기)

- 검색 → UPSERT 1회 (인덱스 업데이트 포함) → 밀리초 단위
- 랭킹 조회 → 캐시 적중 시 DB 쿼리 없음. 미스 시 인덱스 한 번 탐색

### 7.2 쓰기 타이밍 (동기 · AFTER_COMMIT)

- UPSERT 1회는 ~1ms 수준이므로 기본 버전은 **동기 실행**으로 충분
- `@TransactionalEventListener(AFTER_COMMIT) + @Transactional(REQUIRES_NEW)` 리스너 (`GameRankingService` 와 동일 컨벤션)
- 사용자 응답은 이미 HTTP 커밋 단계에 있음 → 체감 지연 영향 미미
- 리스너 실패는 try-catch + `LoggerFactory.getLogger(...).warn(...)` 로 방어 (사용자 검색 결과에 영향 없음)

향후 트래픽이 늘어 동기 처리가 검색 응답 시간을 눈에 띄게 지연시키면 §8.2 인메모리 버퍼링으로 전환.

### 7.3 핫 키워드 경합

단일 인기 키워드(`사랑`)에 초당 수백 건이 몰리면 같은 row lock 경합 발생 가능.

대응:
- 초기 수준(< 100 tps 동일 키워드) 에서는 UPSERT 만으로 충분
- 경합 심화 시 §8.2 인메모리 버퍼링으로 전환

## 8. 확장 가능성

### 8.1 보관 정책

- 전체 누적 테이블이므로 row 수는 "서로 다른 정규화 키워드 종수" 로 상한이 있음 (수만 ~ 수십만 level)
- 폭증 시 `last_searched_at` 기준 180일 미접근 키워드를 `search_count < N` 조건으로 주기 삭제

### 8.2 인메모리 버퍼링 (2단계)

트래픽 임계 초과 시:

1. 리스너에서 DB 직접 쓰기 대신 `ConcurrentHashMap<NormalizedKeyword, LongAdder>` 에 누적
2. 1분 단위 `@Scheduled` 작업이 버퍼를 flush → 단일 UPSERT batch 실행
3. 단일 키워드 핫스팟이 수백 tps 여도 DB UPSERT 는 분당 1회로 축소
4. 앱 재기동 시 누적 내역 일부 손실 허용 (집계 지표이므로 acceptable)

### 8.3 일별 롤업 (3단계)

`bible_search_keyword_daily_stat(stat_date, normalized_keyword, daily_count)` 추가 시 "오늘의 인기 검색어", "이번 주 인기 검색어" 구현 가능.

### 8.4 Zero-result 키워드 추적

검색 결과가 0건인 키워드를 별도 집계하면 "사전/색인 보완" 우선순위 결정 가능.
초기 버전 범위에서는 제외.

## 9. 구현 순서

### Phase 1 (이 문서 범위)
1. `BibleSearchKeyword` 엔티티 (`BaseTimeEntity` 상속) + Repository (네이티브 UPSERT 쿼리)
2. `NormalizedKeyword` VO + 정규화 로직
3. `BibleSearchPerformedEvent` data class
4. `BibleSearchKeywordListener` — `@TransactionalEventListener(AFTER_COMMIT)` + `@Transactional(REQUIRES_NEW)` 동기 리스너
5. **`BibleReader.searchBibleVersesSlice()` 내부**(트랜잭션 경계 안)에서 `publishEvent(BibleSearchPerformedEvent(keyword))` 호출 추가
6. `BibleSearchKeywordApi` + `BibleSearchKeywordApiDocument` (랭킹 조회, `limit` 1~50 검증 → `ServiceError(INVALID_PARAMETER)`)
7. `common/config/CacheConfig.kt` — `@EnableCaching` + Caffeine `CacheManager` 빈 (이 프로젝트에는 기존 `@EnableCaching` 이 없음)
8. `BibleSearchKeywordService.getRanking()` 에 `@Cacheable("bible-search-keyword-ranking", key = "#limit")` 적용
9. 운영 DDL SQL 작성 (§4 기준)
10. 테스트
    - 단위: `NormalizedKeyword` 정규화 로직 (trim, lowercase, 공백 정리, 길이 제한)
    - 통합: `BibleSearchKeywordRepository` UPSERT 멱등성 (같은 키워드 N회 → `search_count = N`)
    - 통합: 검색 → 이벤트 발행 → 리스너 → count 증가가 실제로 일어나는지 (end-to-end)

> 기존 `/api/v1/bibles/**` 가 `SecurityConfig.kt:99` 에서 `permitAll` 처리됨. 새 랭킹 URL 도 같은 prefix 하위이므로 SecurityConfig 변경은 **불필요**.

### Phase 2
1. 인메모리 버퍼링(`ConcurrentHashMap + LongAdder`) + `@Scheduled` 주기 flush
2. 관리자 상세 조회 API (§5.3)
3. 관리자 블랙리스트 (§5.4) — `is_blocked` 컬럼 + PATCH API
4. 보관 정책 배치 (`last_searched_at` 기준 오래된 low-count row 삭제)

### Phase 3
1. 일별 롤업 테이블 (§8.3)
2. 기간별 인기 검색어 API (오늘/이번 주/이번 달)
3. Zero-result 키워드 로그
