# 성경 사전 검색 키워드 집계 · 랭킹 설계 문서

## 구현 상태

설계 완료, 미구현.

대상 화면: `src/main/resources/templates/study/dictionary-list.html`
기존 검색 API: `GET /api/v1/study/dictionaries?keyword=...` (`DictionaryApi.getDictionaries`)

## 1. 요구사항 요약

1. 비로그인/로그인 사용자 모두 성경 사전 검색 가능
2. `dictionary-list.html` 에서 사용자가 입력한 검색어별 검색 횟수 집계
3. 공백 검색(전체 목록 조회)은 집계하지 않음
4. 검색 결과가 1건 이상인 키워드만 PostgreSQL 에 누적 집계
5. 인기 검색어(랭킹) 조회 API 제공
6. Redis 미사용, 로컬 캐시만 사용
7. 1글자 키워드는 사전의 **정확한 표제어와 일치하는 경우에만** 집계

### 1.1 개인정보 처리 방침

- 집계 대상은 **키워드 자체**이며 "누가 검색했는지" 는 기록하지 않는다.
- `DictionarySearchKeyword` 테이블에 `member_id` / `member_uid` / IP 등 사용자 식별 정보 없음.
- 이는 로그인/비로그인 동등 처리 요구사항과 개인정보 최소 수집 원칙에 부합.

### 1.2 화면 요구사항

- `dictionary-list.html` 상단 검색 영역 아래에 **인기 검색어** 섹션을 노출한다.
- 사용자가 인기 검색어를 탭/클릭하면 해당 키워드가 검색창에 채워지고 기존 검색 플로우(`App.startSearch`)를 그대로 재사용한다.
- 랭킹 조회 실패는 사전 검색 기능에 영향을 주지 않으며, UI 는 해당 섹션만 숨기거나 빈 상태로 처리한다.
- 랭킹 영역은 검색 결과 유무와 관계없이 유지하되, 빈 검색 초기 진입 시 가장 먼저 노출되도록 한다.

## 2. 설계 전략

### 2.1 로깅 지점

기존 사전 검색 API에 **집계 부수 효과(side-effect)** 를 추가한다.

- 발행 위치: **`DictionaryService.getDictionaries()` 내부** (⚠️ 중요)
  - `DictionaryService` 는 클래스 레벨 `@Transactional(readOnly = true)` 가 있음
  - `DictionaryApi.getDictionaries()` 는 `@Transactional` 이 없음
  - 따라서 `DictionaryApi` 에서 발행하면 활성 트랜잭션이 없어 `AFTER_COMMIT` 리스너가 발화하지 않음
  - 반드시 `DictionaryService.getDictionaries()` 내부에서 `ApplicationEventPublisher.publishEvent(...)` 호출
  - read-only 트랜잭션도 `AFTER_COMMIT` 이 정상 발화함
- 발행 조건:
  - `keyword` 가 blank 가 아닐 것
  - `pageable.pageNumber == 0` 일 것
  - 검색 결과 `totalElements > 0` 일 것
- 발행 호출은 `runCatching` 으로 감싸 집계 예외가 사전 검색 API 응답 성공 여부에 영향 주지 않도록 방어
- 리스너: `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Transactional(propagation = REQUIRES_NEW)` **동기 실행**
  - 프로젝트 기존 컨벤션인 `BibleSearchKeywordListener` 와 동일 패턴
  - UPSERT 1회 수준이므로 동기 실행으로 충분
  - 리스너 내부 try-catch 로 집계 실패가 사용자 응답에 영향 주지 않도록 방어

이 구조의 이점:
- 검색 트랜잭션이 성공한 경우에만 집계
- 빈 검색/페이지네이션/0건 검색으로 인한 랭킹 왜곡 방지
- 집계가 실패해도 사용자 응답은 정상 반환
- 기존 프로젝트 컨벤션 재사용으로 구현 리스크 최소화

### 2.2 키워드 정규화

집계 일관성을 위해 저장 전 정규화:

- `trim()` → 양끝 공백 제거
- `lowercase()` → 영문 소문자화 (한글은 영향 없음)
- 연속 공백 → 단일 공백
- 1글자 키워드는 **정확한 사전 표제어와 일치할 때만** 집계
- 2글자 이상만 일반 집계 대상으로 허용
- 50자 초과는 집계 제외

정규화된 값은 `normalized_keyword` 컬럼에 저장, 원본 키워드는 `keyword` 컬럼에 별도 보관.

> 성경 사전에는 `욥`, `롯` 처럼 1글자 표제어가 실제로 존재한다. 따라서 Phase 1 부터 "1글자 키워드는 정확한 표제어 일치 시 허용" 을 기본 정책으로 둔다. 단순 1글자 부분검색(노이즈 가능성)은 집계하지 않는다.

### 2.3 동시성: UPSERT + 원자 증분

Read-modify-write 대신 PostgreSQL 원자 연산 사용. Spring Data JPA 에서는 **반드시 `@Modifying` + `nativeQuery = true`** 로 선언한다.

```kotlin
interface DictionarySearchKeywordRepository : JpaRepository<DictionarySearchKeyword, Long> {

    @Modifying
    @Query(
        value = """
            INSERT INTO dictionary_search_keyword
                (normalized_keyword, keyword, search_count, last_searched_at, created_at, updated_at)
            VALUES
                (:normalized, :original, 1, :now, :now, :now)
            ON CONFLICT (normalized_keyword)
            DO UPDATE SET
                search_count     = dictionary_search_keyword.search_count + 1,
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
        SELECT new com.elseeker.study.application.result.DictionarySearchKeywordRankingResult(
            e.keyword, e.searchCount
        )
        FROM DictionarySearchKeyword e
        WHERE e.blocked = false
        ORDER BY e.searchCount DESC, e.lastSearchedAt DESC
        """
    )
    fun findTopRanking(pageable: Pageable): List<DictionarySearchKeywordRankingResult>
}
```

- 단일 DB 라운드트립
- `normalized_keyword` 유니크 제약 기반의 row-level lock 만 획득 → 다른 키워드끼리는 경합 없음
- `SELECT FOR UPDATE` 나 애플리케이션 레벨 락 불필요
- `keyword` 컬럼은 UPSERT 마다 최신 원본으로 덮어쓴다
- 공개 랭킹 노출 제어를 위해 Phase 1 부터 `blocked` 컬럼을 포함한다

## 3. 도메인 구조

기존 `study` 모듈 내에 추가.

```text
study/
├─ domain/model/DictionarySearchKeyword.kt
├─ domain/vo/NormalizedDictionaryKeyword.kt
├─ domain/event/DictionarySearchPerformedEvent.kt
├─ application/service/DictionarySearchKeywordService.kt
├─ application/listener/DictionarySearchKeywordListener.kt
├─ application/result/DictionarySearchKeywordRankingResult.kt
├─ adapter/input/api/client/DictionarySearchKeywordApi.kt
├─ adapter/input/api/client/DictionarySearchKeywordApiDocument.kt
├─ adapter/input/api/client/response/DictionarySearchKeywordRankingResponse.kt
└─ adapter/output/jpa/DictionarySearchKeywordRepository.kt
```

`DictionaryService.getDictionaries()` 내부(기존 `@Transactional(readOnly = true)` 경계 안)에서 `applicationEventPublisher.publishEvent(DictionarySearchPerformedEvent(keyword))` 호출 추가. 이를 위해 `DictionaryService` 생성자에 `ApplicationEventPublisher` 를 주입한다.

### 3.1 주요 코드 스켈레톤

**이벤트 (domain/event):**
```kotlin
data class DictionarySearchPerformedEvent(val keyword: String)
```

**리스너 (application/listener):**
```kotlin
@Component
class DictionarySearchKeywordListener(
    private val dictionarySearchKeywordService: DictionarySearchKeywordService,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onSearchPerformed(event: DictionarySearchPerformedEvent) {
        try {
            dictionarySearchKeywordService.increment(event.keyword)
        } catch (e: Exception) {
            logger.warn("Failed to increment dictionary search keyword count: {}", e.message)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DictionarySearchKeywordListener::class.java)
    }
}
```

- 리스너는 반드시 **별도 `@Component` 빈** 이어야 한다.
- `AFTER_COMMIT` 이므로 `DictionaryService` read-only 트랜잭션이 정상 종료된 뒤에만 실행된다.

**서비스 (application/service):**
```kotlin
@Service
class DictionarySearchKeywordService(
    private val repository: DictionarySearchKeywordRepository,
    private val dictionaryRepository: DictionaryRepository,
) {

    @Transactional
    fun increment(rawKeyword: String) {
        val normalized = NormalizedDictionaryKeyword.ofOrNull(rawKeyword) ?: return
        val original = rawKeyword.trim().take(MAX_KEYWORD_LENGTH)
        if (normalized.isSingleChar() && !dictionaryRepository.existsByExactTermIgnoreCase(original)) {
            return
        }
        repository.upsertCount(normalized.value, original, Instant.now())
    }

    @Cacheable(value = [CacheConfig.CACHE_DICTIONARY_SEARCH_KEYWORD_RANKING], key = "#limit")
    @Transactional(readOnly = true)
    fun getRanking(limit: Int): List<DictionarySearchKeywordRankingResult> {
        if (limit !in 1..50) {
            throwError(ErrorType.INVALID_PARAMETER)
        }
        return repository.findTopRanking(PageRequest.of(0, limit))
    }

    companion object {
        private const val MAX_KEYWORD_LENGTH = 50
    }
}
```

> 원본 키워드(`keyword`) 저장 시에도 반드시 `.trim().take(50)` 으로 길이를 제한한다. 정규화 후 길이가 50 이하더라도 원본 문자열은 50자를 초과할 수 있으므로, 이 방어가 없으면 `VARCHAR(50)` 제약 위반이 발생할 수 있다.

**`DictionaryService.getDictionaries()` 변경 포인트:**
```kotlin
fun getDictionaries(keyword: String?, pageable: Pageable): Page<Dictionary> {
    val page = if (keyword.isNullOrBlank()) {
        dictionaryRepository.findAllOrderByKo(pageable)
    } else {
        dictionaryRepository.findByTermContainingKo(keyword.trim(), pageable)
    }

    if (!keyword.isNullOrBlank() && pageable.pageNumber == 0 && page.totalElements > 0) {
        runCatching {
            applicationEventPublisher.publishEvent(DictionarySearchPerformedEvent(keyword))
        }.onFailure { e ->
            logger.warn("Failed to publish DictionarySearchPerformedEvent for keyword='{}'", keyword, e)
        }
    }

    return page
}
```

**정규화 VO (domain/vo):**
```kotlin
@JvmInline
value class NormalizedDictionaryKeyword private constructor(val value: String) {
    companion object {
        private const val MIN_LENGTH = 1
        private const val MAX_LENGTH = 50

        fun ofOrNull(raw: String): NormalizedDictionaryKeyword? {
            val collapsed = raw.trim().replace(WHITESPACE_REGEX, " ").lowercase()
            if (collapsed.length !in MIN_LENGTH..MAX_LENGTH) return null
            return NormalizedDictionaryKeyword(collapsed)
        }
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    fun isSingleChar(): Boolean = value.length == 1
}
```

**랭킹 결과 DTO (application/result):**
```kotlin
data class DictionarySearchKeywordRankingResult(
    val keyword: String,
    val searchCount: Long,
)
```

**`DictionaryRepository` 보조 메서드 (1글자 표제어 검증용):**
```kotlin
@Query(
    """
    SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
    FROM Dictionary d
    WHERE LOWER(d.term) = LOWER(:term)
    """
)
fun existsByExactTermIgnoreCase(@Param("term") term: String): Boolean
```

## 4. DB 테이블 설계

### 4.1 테이블 정의

```sql
CREATE TABLE dictionary_search_keyword (
    id                  BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    normalized_keyword  VARCHAR(50)  NOT NULL,
    keyword             VARCHAR(50)  NOT NULL,
    search_count        BIGINT       NOT NULL DEFAULT 0,
    blocked             BOOLEAN      NOT NULL DEFAULT false,
    last_searched_at    TIMESTAMP(6) NOT NULL,
    created_at          TIMESTAMP(6) NOT NULL,
    updated_at          TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_dictionary_search_keyword_normalized UNIQUE (normalized_keyword)
);

COMMENT ON TABLE  dictionary_search_keyword                    IS '성경 사전 검색 키워드 누적 집계';
COMMENT ON COLUMN dictionary_search_keyword.normalized_keyword IS '정규화 키워드 (집계 키, 유니크)';
COMMENT ON COLUMN dictionary_search_keyword.keyword            IS '최근 검색 원본 키워드';
COMMENT ON COLUMN dictionary_search_keyword.search_count       IS '누적 검색 횟수';
COMMENT ON COLUMN dictionary_search_keyword.blocked            IS '공개 랭킹 노출 제외 여부';
COMMENT ON COLUMN dictionary_search_keyword.last_searched_at   IS '최근 검색 시각 (UTC)';
```

### 4.2 인덱스

```sql
CREATE INDEX idx_dictionary_search_keyword_count_desc
    ON dictionary_search_keyword (blocked, search_count DESC, last_searched_at DESC);

CREATE INDEX idx_dictionary_search_keyword_last_searched_at
    ON dictionary_search_keyword (last_searched_at DESC);
```

- 랭킹 조회는 `WHERE blocked = false ORDER BY search_count DESC LIMIT N`
- `last_searched_at` 단독 인덱스는 "최근 N일간 인기 검색어" 확장이나 보관 정책 배치에 사용

### 4.3 컬럼 선택 근거

- `normalized_keyword VARCHAR(50)`: 실무 검색 키워드 길이 한계로 충분
- `keyword VARCHAR(50)`: 원본 보존 (표시용/디버깅용)
- `search_count BIGINT`: 장기 누적 대비
- `blocked BOOLEAN`: 관리자 공개 노출 제어 (Phase 1 포함)
- FK 없음: 검색 키워드는 독립 집계 데이터

## 5. API 설계

### 5.1 사전 검색 (기존, 동작만 확장)

`GET /api/v1/study/dictionaries?keyword={q}&page=0&size=50`

- 기존 응답 스펙 유지
- 서비스 내부에서 이벤트 발행 추가
- `keyword` blank 면 전체 목록 조회만 수행하고 집계하지 않음
- `page > 0` 인 추가 스크롤 요청은 집계하지 않음
- `totalCount == 0` 인 검색은 공개 랭킹 집계에서 제외
- 1글자 검색어는 정확한 사전 표제어 일치가 확인될 때만 집계

### 5.2 인기 검색어 조회 (신규)

`GET /api/v1/study/dictionaries/search-keywords/ranking?limit=10`

쿼리 파라미터:
- `limit`: 1~50, 기본 10

응답 예시:

```json
{
  "items": [
    { "rank": 1, "keyword": "바울", "searchCount": 248 },
    { "rank": 2, "keyword": "아브라함", "searchCount": 173 },
    { "rank": 3, "keyword": "은혜", "searchCount": 151 }
  ],
  "refreshedAt": "2026-04-19T10:15:30Z"
}
```

- 권한: `permitAll`
  - 기존 `SecurityConfig.kt` 에서 `/api/v1/study/dictionaries/**` 가 이미 `permitAll` 처리됨
  - 새 랭킹 API 도 같은 prefix 하위이므로 SecurityConfig 변경 불필요
- `keyword` 는 표시용으로 `normalized_keyword` 가 아닌 `keyword`(가장 최근 원본) 를 반환
- `blocked = true` 인 키워드는 제외
- `limit` 범위를 벗어나면 `ServiceError(ErrorType.INVALID_PARAMETER)` 를 던져 400 JSON 응답 반환

### 5.3 관리자 상세 조회 (Phase 2)

`GET /api/v1/admin/dictionaries/search-keywords?page=0&size=50&sort=searchCount,desc`

- 운영자용 페이지네이션 조회 + 키워드 검색
- `blocked` 상태 토글을 위한 목록 화면 제공

### 5.4 관리자 블랙리스트 (공개 랭킹 노출 제어)

공개 랭킹에 부적절한 키워드(욕설·광고성·오타 반복 등)가 노출되는 것을 방지하기 위해 블랙리스트 기능이 필요.

초기 구현은 `DictionarySearchKeyword.blocked` 컬럼 기반으로 충분하며, 본 설계는 **Phase 1 부터 해당 컬럼을 포함** 하는 기준으로 작성한다.

- `PATCH /api/v1/admin/dictionaries/search-keywords/{id}` → `{ "blocked": true }`

## 6. 캐시 전략 (Redis 미사용)

### 6.1 로컬 캐시: Caffeine

- `@Cacheable("dictionary-search-keyword-ranking")` + Caffeine `expireAfterWrite=30s`, `maximumSize=16`
- 랭킹 쿼리는 대부분 동일한 `limit` 값으로 반복 호출 → 캐시 적중률 높음
- TTL 30초 → 인기 검색어가 "실시간"일 필요는 없음
- 인스턴스별 개별 캐시지만, 랭킹은 eventually consistent 한 지표이므로 무방

현재 프로젝트에는 `common/config/CacheConfig.kt` 가 이미 존재하므로 새 캐시 상수와 등록만 추가한다.

```kotlin
@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager = CaffeineCacheManager().apply {
        registerCustomCache(
            CACHE_BIBLE_SEARCH_KEYWORD_RANKING,
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(30))
                .maximumSize(16)
                .build()
        )
        registerCustomCache(
            CACHE_DICTIONARY_SEARCH_KEYWORD_RANKING,
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(30))
                .maximumSize(16)
                .build()
        )
    }

    companion object {
        const val CACHE_BIBLE_SEARCH_KEYWORD_RANKING = "bible-search-keyword-ranking"
        const val CACHE_DICTIONARY_SEARCH_KEYWORD_RANKING = "dictionary-search-keyword-ranking"
    }
}
```

### 6.2 집계 쓰기 측 캐시 (Phase 2)

초기 버전은 UPSERT 직접 호출.
트래픽이 커지면 메모리 버퍼 + 주기 flush 적용 (§8.2 참조).

## 7. 성능 · 동시성 고려

### 7.1 예상 부하 (초기)

- 사전 검색 → UPSERT 1회 (첫 페이지, 결과 존재 시에만)
- 랭킹 조회 → 캐시 적중 시 DB 쿼리 없음. 미스 시 인덱스 한 번 탐색

### 7.2 쓰기 타이밍 (동기 · AFTER_COMMIT)

- UPSERT 1회는 밀리초 단위이므로 기본 버전은 **동기 실행**으로 충분
- `@TransactionalEventListener(AFTER_COMMIT) + @Transactional(REQUIRES_NEW)` 리스너 적용
- 사용자 응답은 이미 정상 검색 흐름 안에서 처리되고, 집계 실패는 로그만 남긴다

### 7.3 핫 키워드 경합

인기 용어(`예수`, `바울`)에 요청이 집중되면 같은 row lock 경합 가능.

대응:
- 초기 수준에서는 UPSERT 만으로 충분
- 경합 심화 시 §8.2 인메모리 버퍼링으로 전환

## 8. 확장 가능성

### 8.1 보관 정책

- 전체 누적 테이블이므로 row 수는 "서로 다른 정규화 키워드 종수" 로 상한이 있음
- 장기적으로 `last_searched_at` 기준 180일 이상 미사용 + `search_count < N` row 삭제 가능

### 8.2 인메모리 버퍼링 (2단계)

트래픽 임계 초과 시:

1. 리스너에서 DB 직접 쓰기 대신 `ConcurrentHashMap<NormalizedDictionaryKeyword, LongAdder>` 에 누적
2. 1분 단위 `@Scheduled` 작업이 버퍼를 flush → batch UPSERT 실행
3. 단일 키워드 핫스팟이 있어도 DB 쓰기 횟수를 줄일 수 있음
4. 앱 재기동 시 일부 손실 허용 (집계 지표이므로 acceptable)

### 8.3 일별 롤업 (3단계)

`dictionary_search_keyword_daily_stat(stat_date, normalized_keyword, daily_count)` 추가 시 "오늘의 인기 검색어", "이번 주 인기 검색어" 구현 가능.

### 8.4 Zero-result 키워드 추적

Phase 1 공개 랭킹은 0건 검색을 제외하지만, 운영 관점에서는 "사용자가 찾았으나 없는 용어" 가 유의미할 수 있다.

향후 별도 테이블 예시:
- `dictionary_search_zero_result_keyword`

이를 통해 사전 콘텐츠 보강 우선순위 산정 가능.

## 9. 구현 순서

### Phase 1 (이 문서 범위)
1. `DictionarySearchKeyword` 엔티티 + Repository (네이티브 UPSERT 쿼리)
2. `NormalizedDictionaryKeyword` VO + 정규화 로직
3. `DictionarySearchPerformedEvent` data class
4. `DictionarySearchKeywordListener` — `@TransactionalEventListener(AFTER_COMMIT)` + `@Transactional(REQUIRES_NEW)` 동기 리스너
5. `DictionaryService.getDictionaries()` 내부에서 `publishEvent(DictionarySearchPerformedEvent(keyword))` 호출 추가
6. `DictionaryRepository.existsByExactTermIgnoreCase()` 추가 (1글자 표제어 검증용)
7. `DictionarySearchKeywordApi` + `DictionarySearchKeywordApiDocument` (랭킹 조회, `limit` 1~50 검증)
8. `blocked BOOLEAN NOT NULL DEFAULT false` 포함 DDL 작성
9. `CacheConfig.kt` 에 `dictionary-search-keyword-ranking` 캐시 등록
10. `dictionary-list.html` 에 인기 검색어 섹션 추가
11. `dictionary-list.js` 에 랭킹 조회/클릭 검색 연동 추가
12. 운영 DDL SQL 작성 (§4 기준)
13. 테스트
    - 단위: `NormalizedDictionaryKeyword` 정규화 로직
    - 통합: `DictionarySearchKeywordRepository` UPSERT 멱등성
    - 통합: `DictionaryService.getDictionaries(keyword, page=0)` 호출 시 이벤트 발행 후 count 증가
    - 통합: `keyword=null`, `page>0`, `totalCount=0` 조건에서는 집계되지 않는지 검증
    - 통합: 1글자 검색어는 정확한 표제어 일치 시에만 집계되는지 검증

> 기존 `/api/v1/study/dictionaries/**` 가 `SecurityConfig.kt` 에서 `permitAll` 처리됨. 새 랭킹 URL 도 같은 prefix 하위이므로 SecurityConfig 변경은 불필요.

### Phase 2
1. 관리자 검색 키워드 목록/차단 UI
2. 인메모리 버퍼링(`ConcurrentHashMap + LongAdder`) + `@Scheduled` 주기 flush
3. 보관 정책 배치 (`last_searched_at` 기준 오래된 low-count row 삭제)

### Phase 3
1. 일별 롤업 테이블 (§8.3)
2. 기간별 인기 검색어 API (오늘/이번 주/이번 달)
3. Zero-result 키워드 로그 분리
