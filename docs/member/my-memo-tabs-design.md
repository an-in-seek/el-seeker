# 나의 메모 — 책 / 장 / 절 탭 분리 설계

## 구현 상태

설계 완료, 미구현.

---

## 1. 배경 & 문제정의

### 1.1 현재 상태

`/web/member/my-memo` (`my-memo.html`) 페이지는 **구절 메모(`BibleVerseMemo`) 만** 조회한다.

- API: `GET /api/v1/bibles/my-memos` → `BibleMemoService.getMyMemos()` → `BibleMemoRepository` 의 verse 메모만 페치
- 카드: `bookName chapter:verse` + 본문, 클릭 시 `/web/bible/verse?...&verseNumber=...` 으로 이동
- 필터: 번역본(`translationId`) → 책(`bookOrder`) 2단계 드롭다운

### 1.2 추가된 도메인

이미 백엔드에는 **책 메모 / 장 메모** 도메인이 존재한다:

| 엔티티 | 키 | 용도 화면 |
|---|---|---|
| `BibleBookMemo` | `(member, translation, book)` | `chapter-list.html` 책 단위 메모 |
| `BibleChapterMemo` | `(member, translation, book, chapter)` | `verse-list.html` 장 단위 메모 |
| `BibleVerseMemo` | `(member, translation, book, chapter, verse)` | `verse-list.html` 구절 단위 메모 |

그러나 **책/장 메모의 회원-범위 목록 조회 API 와 화면은 미구현** 상태다. 회원이 작성한 책 메모·장 메모를 한곳에서 확인할 동선이 없다.

### 1.3 요구사항

- **R1.** `my-memo.html` 을 `[책 메모 | 장 메모 | 절 메모]` 3개 탭으로 분리한다.
- **R2.** 각 탭은 자신의 도메인(`Bible{Book|Chapter|Verse}Memo`)을 독립적으로 조회·필터·페이지네이션 한다.
- **R3.** 카드 클릭 시 메모를 **수정/확인 가능한 원래 화면**으로 이동한다 (책 → `chapter`, 장 → `verse(장)`, 절 → `verse(장+절)`).
- **R4.** 탭 선택은 URL 쿼리(`?tab=book|chapter|verse`)로 보존하여 새로고침·공유 가능.
- **R5.** 빈 상태 메시지·CTA 는 탭의 의미에 맞게 분기한다.
- **비기능**: 구절 메모의 기존 동작·URL·API 는 깨지지 않는다 (호환).

---

## 2. 변경 범위 요약

### 2.1 백엔드 — 신규/수정

| 파일 | 종류 | 변경 |
|---|---|---|
| `BibleBookMemoRepository.kt` | 수정 | 회원 범위 페치/카운트/distinct 쿼리 추가 |
| `BibleChapterMemoRepository.kt` | 수정 | 동일 패턴 |
| `BibleBookMemoService.kt` | 수정 | `getMyBookMemos`, `getMemoTranslations`, `getMemoBookList` 추가 |
| `BibleChapterMemoService.kt` | 수정 | 동일 패턴 |
| `BibleMyBookMemoApi.kt` | **신규** | `GET /api/v1/bibles/my-book-memos` 외 2개 |
| `BibleMyChapterMemoApi.kt` | **신규** | `GET /api/v1/bibles/my-chapter-memos` 외 2개 |
| `BibleBookMemoResult.kt` | **신규** | `BookMemoSlice`, `BookMemoItem`, 공통 필터 DTO |
| `BibleChapterMemoResult.kt` | **신규** | `ChapterMemoSlice`, `ChapterMemoItem` |
| `SecurityConfig.kt` | 수정 | 신규 my-* 경로 `.authenticated()` 등록 |

> `BibleMyMemoApi` (절 메모) 는 변경 없음. 단 일관성을 위해 향후 `/api/v1/bibles/my-verse-memos` 별칭을 두는 것은 Phase 3 대안으로 검토 (현재 `/my-memos` 도 그대로 유지).

### 2.2 프론트엔드 — 수정 (3 파일)

| 파일 | 변경 |
|---|---|
| `templates/member/my-memo.html` | 탭 네비게이션 추가, 단일 컨테이너 유지(필터+리스트+빈상태+더보기 공유) |
| `static/js/member/my-memo.js` | `TAB_SPEC` 도입, 탭별 fetch URL · 카드 빌더 · 빈 메시지 분기, URL 쿼리 동기화 |
| `static/css/member/my-memo.css` | 탭 스타일 추가 (mypage 탭 톤 재사용), 카드 변형 스타일 |

### 2.3 변경 없는 파일

- `BibleVerseMemo`, `BibleBookMemo`, `BibleChapterMemo` 엔티티 자체
- `MemberWebController` (라우팅 그대로 `/my-memo`)
- 기존 단건 CRUD API (`/book-memo`, `/chapter-memo`, `/verses/{n}/memo`) — 변경 없음

---

## 3. UX & 화면 구조

### 3.1 와이어프레임

```
┌─ 히어로 ────────────────────────────────────────┐
│   "나의 메모"                                   │
│   설명문                                        │
│   (히어로 합계 배지는 제거 — 탭별 카운트로 이관)│
└─────────────────────────────────────────────────┘
┌─ 메인 카드 ─────────────────────────────────────┐
│ [ 책 메모 (3) │ 장 메모 (12) │ 절 메모 (47) ]  │  ← 탭
│ ─────────────────────────────────────────────── │
│  번역본 ▼              성경 ▼                   │  ← 필터
│ ─────────────────────────────────────────────── │
│  [메모 카드]                                    │
│  [메모 카드]                                    │
│  [메모 카드]                                    │
│ ─────────────────────────────────────────────── │
│            [   더보기   ]                       │
└─────────────────────────────────────────────────┘
```

### 3.2 탭 동작 규칙

| 항목 | 규칙 |
|---|---|
| 활성 탭 | URL `?tab` 우선 → 없으면 `verse` (기본값, 기존 동선 보존) |
| 탭 전환 | `pushState` 로 `?tab=` 갱신, 필터 상태도 같이 초기화 (탭 간 컨텍스트는 공유하지 않음) |
| 카운트 배지 | 탭 라벨 옆 작은 숫자, 최초 진입 시 각 탭의 totalCount 를 **병렬로 1회 프리페치** |
| 빈 상태 | 탭별 메시지 + CTA (§6 참고) |
| 더보기 | 탭별 독립 페이지네이션 (state 가 탭별로 분리) |

### 3.3 카드 → 원본 화면 매핑

| 탭 | 클릭 시 이동 URL | 비고 |
|---|---|---|
| 책 메모 | `/web/bible/chapter?translationId=&bookOrder=&from=my-memo` | 책의 장 목록(`chapter-list.html`) — 책 메모 패널이 그곳에 있음 |
| 장 메모 | `/web/bible/verse?translationId=&bookOrder=&chapterNumber=&from=my-memo` | 장 진입 → 장 메모 버튼이 그곳에 있음 |
| 절 메모 | `/web/bible/verse?translationId=&bookOrder=&chapterNumber=&verseNumber=&from=my-memo` | 기존 동일, `from` 값만 `mypage` → `my-memo` 로 정정 |

> `from=my-memo` 쿼리는 현재 어떤 분기 로직도 갖지 않는 **마커**다 (grep 결과 `verse-list.js` 등 어디에서도 `from` 파라미터를 해석하지 않음). Phase 3 에서 "뒤로 가기 → `/web/member/my-memo?tab=…`" 복귀 동작을 추가할 자리표시자.
>
> `mypage → my-memo` 변경은 **마커 값 정정**일 뿐, 호환성 영향 없음 (어떤 코드도 이 값을 읽지 않음).
>
> URL 경로 검증: `BibleWebController` 가 `@GetMapping("/chapter")` 와 `@GetMapping("/verse")` 만 매핑 — `/chapter-list`, `/verse-list` 가 아님. 위 표의 경로는 컨트롤러 매핑 기준.

---

## 4. 백엔드 설계

### 4.1 Repository — 추가 쿼리

`BibleBookMemoRepository` (현재는 단건 조회만 존재) 에 추가:

```kotlin
fun countByMemberUid(memberUid: UUID): Long
fun countByMemberUidAndTranslationId(memberUid: UUID, translationId: Long): Long
fun countByMemberUidAndTranslationIdAndBookOrder(
    memberUid: UUID, translationId: Long, bookOrder: Int
): Long

fun findAllByMemberUid(memberUid: UUID, pageable: Pageable): Slice<BibleBookMemo>
fun findAllByMemberUidAndTranslationId(
    memberUid: UUID, translationId: Long, pageable: Pageable
): Slice<BibleBookMemo>
fun findAllByMemberUidAndTranslationIdAndBookOrder(
    memberUid: UUID, translationId: Long, bookOrder: Int, pageable: Pageable
): Slice<BibleBookMemo>

@Query("SELECT DISTINCT m.translationId FROM BibleBookMemo m " +
       "WHERE m.member.uid = :memberUid ORDER BY m.translationId")
fun findDistinctTranslationIdsByMemberUid(memberUid: UUID): List<Long>

@Query("SELECT DISTINCT m.bookOrder FROM BibleBookMemo m " +
       "WHERE m.member.uid = :memberUid AND m.translationId = :translationId " +
       "ORDER BY m.bookOrder")
fun findDistinctBookOrdersByMemberUidAndTranslationId(
    memberUid: UUID, translationId: Long
): List<Int>
```

`BibleChapterMemoRepository` 에는 동일 시그니처(타입만 `BibleChapterMemo`) 로 추가한다. **책 필터 + 장 그룹** 까지 좁히는 `bookOrder` 필터 메서드도 같은 모양:

```kotlin
fun findAllByMemberUidAndTranslationIdAndBookOrder(
    memberUid: UUID, translationId: Long, bookOrder: Int, pageable: Pageable
): Slice<BibleChapterMemo>
```

> 정렬은 모두 `Sort.by(DESC, "updatedAt")` 을 서비스 레이어에서 부여한다 (절 메모 서비스 패턴과 동일).

### 4.2 Service — 추가 메서드

`BibleBookMemoService` 에 추가 (절 메모 서비스의 `getMyMemos` / `getMemoTranslations` / `getMemoBookList` 와 동일 형태):

```kotlin
@Transactional(readOnly = true)
fun getMyBookMemos(
    memberUid: UUID,
    pageable: Pageable,
    translationId: Long? = null,
    bookOrder: Int? = null
): BibleBookMemoResult.BookMemoSlice

@Transactional(readOnly = true)
fun getMemoTranslations(memberUid: UUID): List<BibleMemoResult.MemoTranslationItem>

@Transactional(readOnly = true)
fun getMemoBookList(
    memberUid: UUID, translationId: Long
): List<BibleMemoResult.MemoBookItem>
```

`BibleChapterMemoService` 도 동일 시그니처(반환형만 `ChapterMemoSlice`).

### 4.3 결과 DTO

`BibleMemoResult.MemoTranslationItem` / `MemoBookItem` 은 **3 탭 공통 필터 응답** 으로 그대로 재사용한다 (`translationId/translationName`, `bookOrder/bookName` 만 들고 있음).

신규 슬라이스/아이템 — `bible/domain/result/BibleBookMemoResult.kt`:

```kotlin
object BibleBookMemoResult {
    data class BookMemoSlice(
        val content: List<BookMemoItem>,
        val hasNext: Boolean,
        val size: Int,
        val number: Int,
        val totalCount: Long?
    )

    data class BookMemoItem(
        val bookMemoId: Long,
        val translationId: Long,
        val bookOrder: Int,
        val bookName: String,
        val content: String,
        val updatedAt: Instant
    ) {
        companion object {
            fun from(memo: BibleBookMemo, bookName: String) = BookMemoItem(
                bookMemoId = memo.id ?: 0,
                translationId = memo.translationId,
                bookOrder = memo.bookOrder,
                bookName = bookName,
                content = memo.content,
                updatedAt = memo.updatedAt
            )
        }
    }
}
```

`ChapterMemoItem` 은 `chapterNumber: Int` 추가, `chapterMemoId` 로 ID 명명. 책 이름 해석은 `BibleMemoService.resolveBookNames()` 와 동일 패턴(번역본 그룹화 + `findByTranslationIdAndBookOrderIn` 1회) 으로 처리.

### 4.4 API — 신규 컨트롤러

`bible/adapter/input/api/client/BibleMyBookMemoApi.kt` (절 메모용 `BibleMyMemoApi` 와 1:1 대칭):

```kotlin
@RestController
@RequestMapping("/api/v1/bibles/my-book-memos")
class BibleMyBookMemoApi(
    private val bibleBookMemoService: BibleBookMemoService
) {
    @GetMapping("/translations")
    fun translations(@AuthenticationPrincipal principal: JwtPrincipal):
        ResponseEntity<List<BibleMemoResult.MemoTranslationItem>> = ...

    @GetMapping("/books")
    fun books(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam translationId: Long
    ): ResponseEntity<List<BibleMemoResult.MemoBookItem>> = ...

    @GetMapping
    fun list(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) translationId: Long?,
        @RequestParam(required = false) bookOrder: Int?
    ): ResponseEntity<BibleBookMemoResult.BookMemoSlice> = ...
}
```

`BibleMyChapterMemoApi` 도 동일 — 단 `bookOrder` 필터까지만 받는다 (`chapterNumber` 필터는 R5 범위 외).

#### 응답 스펙 (3 탭 비교)

| 필드 | book | chapter | verse |
|---|---|---|---|
| `*MemoId` | bookMemoId | chapterMemoId | memoId |
| `translationId` | ✓ | ✓ | ✓ |
| `bookOrder` / `bookName` | ✓ | ✓ | ✓ |
| `chapterNumber` | — | ✓ | ✓ |
| `verseNumber` | — | — | ✓ |
| `content`, `updatedAt` | ✓ | ✓ | ✓ |

`MemoSlice` 는 모두 `{ content, hasNext, size, number, totalCount? }` 동일 형태.

### 4.5 SecurityConfig

**현재 상태 검증** (`SecurityConfig.kt:79-87`):

- `/api/v1/bibles/**` 가 `permitAll()` 블록에 존재 (line 99)
- 위쪽 `.authenticated()` 블록에 **명시적으로 등록된** 경로만 실제 보호:
  - `/chapter-memo`, `/book-memo`, `/state`, `/highlights` 등 단건 CRUD 경로는 이미 등록됨 ✅
  - `/api/v1/bibles/my-memos/**` 는 **등록되지 않음** — 현재 `permitAll` 아래에 있으나, JWT 필터가 principal 을 채워주는 덕에 정상 브라우저 세션에서는 우연히 동작
- 토큰이 없는 직접 호출 시 `@AuthenticationPrincipal JwtPrincipal` 이 `null` → `NullPointerException`

**변경**: 기존 leakage 를 함께 교정하여 신규 2종과 함께 `.authenticated()` 블록에 추가:

```kotlin
// SecurityConfig.kt line 85~87 부근 (기존 chapter-memo / book-memo 목록 다음에 추가)
"/api/v1/bibles/my-memos/**",          // 기존 경로 누락 등록 (bug fix)
"/api/v1/bibles/my-book-memos/**",     // 신규
"/api/v1/bibles/my-chapter-memos/**"   // 신규
```

> 테스트 영향: `/my-memos/**` 에 대해 미인증 요청이 현재는 200/NPE, 변경 후 401 이 된다. 프론트는 `fetchWithAuthRetry` 를 쓰므로 토큰 만료 갱신은 정상 동작 — 회귀 없음.

### 4.6 단위 테스트

- `BibleBookMemoServiceTest` — `getMyBookMemos` 페이지네이션/필터/카운트
- `BibleChapterMemoServiceTest` — 동일
- `BibleMyBookMemoApiIntegrationTest` — 401, 빈 결과, 필터 조합 응답
- `BibleMyChapterMemoApiIntegrationTest` — 동일

---

## 5. 프론트엔드 설계

### 5.1 HTML 변경 — `my-memo.html`

#### 현재 → 변경 차이

| 영역 | 현재 | 변경 |
|---|---|---|
| 히어로 배지 `#myMemoCountBadge` | 합계 카운트 1개 | **삭제** (탭 라벨 배지로 이전) |
| 탭 네비게이션 | 없음 | `#myMemoTabBook/Chapter/Verse` **신규** |
| 빈 상태 텍스트 | 정적 (`성경 본문에서…`) | `id` 부여 후 JS 가 탭별 메시지 주입 |
| 빈 상태 CTA `<a>` | 정적 href | `id="myMemoEmptyCta"` + JS 갱신 |
| 필터/스켈레톤/리스트/더보기 | 단일 인스턴스 | **그대로 단일 인스턴스 공유** (탭 전환 시 내용만 갈아끼움) |

#### 카드 내부 마크업

```html
<article class="card card-panel card-soft my-memo-info-card border-0 shadow-sm">
    <div class="card-body p-4">

        <!-- 탭 네비게이션 -->
        <nav class="my-memo-tabs" role="tablist" aria-label="메모 종류">
            <button id="myMemoTabBook"    class="my-memo-tab" role="tab" data-tab="book">
                책 메모 <span class="my-memo-tab-badge d-none" id="myMemoTabBookBadge"></span>
            </button>
            <button id="myMemoTabChapter" class="my-memo-tab" role="tab" data-tab="chapter">
                장 메모 <span class="my-memo-tab-badge d-none" id="myMemoTabChapterBadge"></span>
            </button>
            <button id="myMemoTabVerse"   class="my-memo-tab active" role="tab"
                    data-tab="verse" aria-selected="true">
                절 메모 <span class="my-memo-tab-badge d-none" id="myMemoTabVerseBadge"></span>
            </button>
        </nav>

        <!-- 필터 (공유) -->
        <div id="myMemoFilter" class="my-memo-filter d-none">
            <select id="myMemoTranslationFilter" class="form-select">…</select>
            <select id="myMemoBookFilter" class="form-select" disabled>…</select>
        </div>

        <div id="myMemoSkeleton" class="my-memo-skeleton-group">…</div>
        <div id="myMemoList" class="my-memo-list" role="tabpanel"></div>
        <div id="myMemoEmpty" class="my-memo-empty d-none">
            <div class="my-memo-empty-icon" aria-hidden="true">📝</div>
            <p class="my-memo-empty-title" id="myMemoEmptyTitle"></p>
            <p class="my-memo-empty-desc"  id="myMemoEmptyDesc"></p>
            <a id="myMemoEmptyCta" class="btn btn-primary"></a>
        </div>

        <button id="myMemoMore" class="btn btn-outline-secondary w-100 mt-3 d-none">
            더보기
        </button>
    </div>
</article>
```

> 히어로(`my-memo-hero`) 의 합계 카운트 배지(`#myMemoCountBadge`) 는 **삭제** — 카운트는 탭 라벨로 이전된다. 단일 카운트가 의미를 잃기 때문.

### 5.2 JS — `my-memo.js`

#### 5.2.1 탭 명세 테이블

탭별 차이를 한 곳에 모아 분기를 단순화한다.

```js
const TAB_SPEC = {
    book: {
        listEndpoint:        "/api/v1/bibles/my-book-memos",
        translationsEndpoint:"/api/v1/bibles/my-book-memos/translations",
        booksEndpoint:       "/api/v1/bibles/my-book-memos/books",
        idField: "bookMemoId",
        cardRef: (m) => m.bookName,                                 // "창세기"
        cardHref: (m) =>
            `/web/bible/chapter?translationId=${m.translationId}` +
            `&bookOrder=${m.bookOrder}&from=my-memo`,
        empty: {
            title: "아직 작성한 책 메모가 없습니다",
            desc:  "성경 책 페이지에서 책 전체에 대한 메모를 남겨보세요.",
            ctaText: "성경 책 목록으로 가기",
            ctaHref: "/web/bible/translation",
        },
    },
    chapter: {
        listEndpoint:        "/api/v1/bibles/my-chapter-memos",
        translationsEndpoint:"/api/v1/bibles/my-chapter-memos/translations",
        booksEndpoint:       "/api/v1/bibles/my-chapter-memos/books",
        idField: "chapterMemoId",
        cardRef: (m) => `${m.bookName} ${m.chapterNumber}장`,        // "창세기 1장"
        cardHref: (m) =>
            `/web/bible/verse?translationId=${m.translationId}` +
            `&bookOrder=${m.bookOrder}&chapterNumber=${m.chapterNumber}&from=my-memo`,
        empty: {
            title: "아직 작성한 장 메모가 없습니다",
            desc:  "성경 장 화면에서 그 장에 대한 묵상을 남겨보세요.",
            ctaText: "성경 읽으러 가기",
            ctaHref: "/web/bible/translation",
        },
    },
    verse: {
        listEndpoint:        "/api/v1/bibles/my-memos",
        translationsEndpoint:"/api/v1/bibles/my-memos/translations",
        booksEndpoint:       "/api/v1/bibles/my-memos/books",
        idField: "memoId",
        cardRef: (m) => `${m.bookName} ${m.chapterNumber}:${m.verseNumber}`,
        cardHref: (m) =>
            `/web/bible/verse?translationId=${m.translationId}` +
            `&bookOrder=${m.bookOrder}&chapterNumber=${m.chapterNumber}` +
            `&verseNumber=${m.verseNumber}&from=my-memo`,
        empty: {
            title: "아직 작성한 메모가 없습니다",
            desc:  "성경 본문에서 마음에 와닿은 구절에 메모를 남겨보세요.",
            ctaText: "성경 읽으러 가기",
            ctaHref: "/web/bible/translation",
        },
    },
};
```

#### 5.2.2 상태 모델

탭별 페이지네이션·필터·로딩 상태를 분리한다. 탭 전환 시 다른 탭의 상태를 잃지 않게 하기 위함.

```js
const createTabState = () => ({
    page: 0, hasNext: false, loading: false,
    translationFilter: null, bookFilter: null,
    rendered: false,                  // 첫 진입 여부
});
const state = {
    activeTab: "verse",               // URL 쿼리로 덮어씀
    byTab: { book: createTabState(), chapter: createTabState(), verse: createTabState() },
    counts: { book: null, chapter: null, verse: null },
};
const cur = () => state.byTab[state.activeTab];
const spec = () => TAB_SPEC[state.activeTab];
```

#### 5.2.3 진입 시퀀스

```
1) checkAuthStatus → 미인증이면 /web/auth/login?returnUrl=/web/member/my-memo
2) 인증 OK:
   2-1) URL 쿼리 ?tab 파싱 → state.activeTab 설정 (기본 "verse")
   2-2) 모든 탭 카운트 프리페치 (병렬: 3개 list?size=1 호출, totalCount 만 사용)
        → 탭 라벨 배지 갱신
   2-3) 활성 탭의 loadTranslations() + loadList() 실행
3) 탭 클릭 → switchTab(target):
        - state.activeTab 변경 + URL pushState ?tab=
        - 모든 탭 버튼의 aria-selected / active 클래스 토글
        - 필터 select 옵션 새로 로드 (탭마다 번역본/책 분포가 다름)
        - 리스트/빈상태/더보기 새로 렌더
```

**aria-selected / active 토글**: HTML 의 `aria-selected="true"` 는 초기값일 뿐, JS 가 매 전환마다 세 탭 모두 업데이트해야 한다:

```js
function applyTabActive(tab) {
    for (const [key, btn] of Object.entries(tabButtons)) {
        const on = key === tab;
        btn.classList.toggle("active", on);
        btn.setAttribute("aria-selected", on ? "true" : "false");
    }
}
```

> 카운트 프리페치는 R3(탭별 카운트 배지) 의 핵심. `size=1` 의 첫 페이지만 호출하면 `totalCount` 가 응답에 포함되므로 추가 카운트 전용 API 가 필요 없다 (서비스의 `pageNumber == 0` 분기 로직이 자동 처리).
>
> **비용 인식**: `size=1` 도 메모 1건을 실제로 페치한다 (book name 해석까지 포함). 3 탭 × 프리페치 = 3 DB round-trip. 규모가 커지면 별도 `GET /count` 엔드포인트로 최적화 가능 (Phase 3).

#### 5.2.4 리스트 로드 — 단일 함수

```js
async function loadList(append = false) {
    const t = cur(); const s = spec();
    if (t.loading) return;
    const requestedTab = state.activeTab;          // race-guard 용 스냅샷
    t.loading = true;

    let url = `${s.listEndpoint}?page=${t.page}&size=${PAGE_SIZE}`;
    if (t.translationFilter != null) url += `&translationId=${t.translationFilter}`;
    if (t.bookFilter != null)        url += `&bookOrder=${t.bookFilter}`;

    const response = await fetchWithAuthRetry(url, {
        credentials: "include",
        headers: { Accept: "application/json" },
    });
    // Race guard: 응답 대기 중 탭이 바뀌었으면 결과를 버림 (다른 탭 DOM 을 덮어쓰지 않음)
    if (state.activeTab !== requestedTab) { t.loading = false; return; }
    if (response.status === 401) { redirectToLogin(); return; }
    if (!response.ok) { renderError(); t.loading = false; return; }

    const data = await response.json();
    if (state.activeTab !== requestedTab) { t.loading = false; return; }

    renderMemos(data.content, append);             // createCard 반복 호출, append 플래그 처리
    t.hasNext = data.hasNext === true;
    if (data.totalCount != null) {
        state.counts[requestedTab] = data.totalCount;
        updateTabBadge(requestedTab);
    }
    t.loading = false;
    t.rendered = true;
}
```

> **Race condition**: 사용자가 탭을 빠르게 전환하면 이전 탭의 응답이 나중에 도착해 현재 탭 DOM 을 덮어쓸 수 있다. 응답 수신 직후 `state.activeTab` 을 재확인하여 다른 탭이면 버린다. AbortController 를 쓰는 대안도 있으나, guard 1줄이 더 단순.

#### 5.2.5 카드 빌더 — 공통 + 분기

```js
function createCard(memo) {
    const s = spec();
    const a = document.createElement("a");
    a.className = "my-memo-card";
    a.href = s.cardHref(memo);
    // header: ref(span) + date(span)
    // body: content
    // 책 메모는 본문이 길 수 있으므로 -webkit-line-clamp:3 유지
    return a;
}
```

#### 5.2.6 URL 쿼리 동기화

- 탭 변경 시: `history.pushState({}, "", "?tab=" + tab)` — 필터값은 쿼리에 보존하지 않음 (단순화 우선, 향후 확장).
- 뒤로 가기(`popstate`): URL 의 `?tab` 을 다시 읽어 `switchTab` 트리거. 이미 렌더된 탭이면 캐시 활용.

#### 5.2.7 인증 401 분기

`fetchWithAuthRetry` 가 갱신을 1회 시도. 그래도 401 이면 `redirectToLogin()` (returnUrl 에 `?tab=` 포함). `loadTranslations` / `loadBooks` 도 동일.

### 5.3 CSS 추가 — `my-memo.css`

```css
/* 탭 네비게이션 — mypage 탭 톤 재사용 */
.my-memo-tabs {
    display: flex;
    gap: 0.25rem;
    border-bottom: 1px solid #e2e8f0;
    margin-bottom: 1rem;
}
.my-memo-tab {
    flex: 1;
    background: transparent;
    border: 0;
    padding: 0.65rem 0.5rem;
    font-size: 0.95rem;
    font-weight: 600;
    color: #64748b;
    border-bottom: 2px solid transparent;
    transition: color .15s, border-color .15s;
    cursor: pointer;
}
.my-memo-tab.active {
    color: var(--my-memo-deep);
    border-bottom-color: var(--my-memo-accent);
}
.my-memo-tab-badge {
    display: inline-block;
    margin-left: 0.25rem;
    padding: 0 0.4rem;
    font-size: 0.7rem;
    font-weight: 700;
    border-radius: 999px;
    background: #eff6ff;
    color: #2563eb;
    min-width: 1.5rem;
}
@media (hover: hover) and (pointer: fine) {
    .my-memo-tab:hover { color: var(--my-memo-deep); }
}
```

> 카드 본문 (`.my-memo-card-content`) 은 책 메모의 긴 본문을 위해 `-webkit-line-clamp: 3` 유지. ref 영역만 탭별 길이 차이가 생기므로 줄바꿈 허용 (`white-space: normal`).

### 5.4 캐시 버스팅

`my-memo.html` 의 `<head>` `th:replace` 인자와 `<script>` 태그:

```
my-memo.css?v=2.0  →  ?v=3.0
my-memo.js?v=1.2   →  ?v=2.0
```

마이너 충돌 회피를 위해 메이저 bump.

---

## 6. 빈 상태 메시지 정책

| 탭 | 제목 | 설명 | CTA 라벨 | CTA 링크 |
|---|---|---|---|---|
| 책 | 아직 작성한 책 메모가 없습니다 | 성경 책 페이지에서 책 전체에 대한 메모를 남겨보세요. | 성경 책 목록으로 가기 | `/web/bible/translation` |
| 장 | 아직 작성한 장 메모가 없습니다 | 성경 장 화면에서 그 장에 대한 묵상을 남겨보세요. | 성경 읽으러 가기 | `/web/bible/translation` |
| 절 | 아직 작성한 메모가 없습니다 | 성경 본문에서 마음에 와닿은 구절에 메모를 남겨보세요. | 성경 읽으러 가기 | `/web/bible/translation` |

> CTA 링크는 모두 번역본 선택 화면 — 책/장 메모를 만들려면 결국 번역본 → 책 → 장 진입이 필요하므로 동일 시작점 사용. 이후 단계 디테일은 향후 책별 딥링크 라우팅으로 보강.

---

## 7. 구현 순서

### Phase 1 — 백엔드

1. `BibleBookMemoRepository` / `BibleChapterMemoRepository` 에 회원범위 쿼리 8 종 추가
2. `BibleBookMemoService.getMyBookMemos` / `getMemoTranslations` / `getMemoBookList` 추가 (절 메모 서비스 패턴 복제)
3. `BibleChapterMemoService` 동일 추가
4. `BibleBookMemoResult` / `BibleChapterMemoResult` DTO 신규
5. `BibleMyBookMemoApi` / `BibleMyChapterMemoApi` 신규 (3 엔드포인트씩)
6. `SecurityConfig` 에 `/my-book-memos/**`, `/my-chapter-memos/**` 등록
7. 서비스 단위 테스트 + API 통합 테스트

### Phase 2 — 프론트

8. `my-memo.html` 탭 네비게이션 / 빈 상태 슬롯 추가, 합계 배지 제거
9. `my-memo.js` `TAB_SPEC` 테이블 도입, 상태 분리, URL 쿼리 동기화
10. `my-memo.css` 탭 스타일 추가
11. 캐시 버스팅 bump

### Phase 3 — 회귀 보강 (선택)

12. `verse-list.js` / `chapter-list.js` 에서 `?from=my-memo` 처리 → top-nav back 시 `/web/member/my-memo?tab=…` 로 복귀
13. 필터값도 URL 쿼리에 보존 (`?tab=verse&translationId=1&bookOrder=3`) — 공유/북마크 시 필터 상태까지 복원

---

## 8. 검증 체크리스트

### 8.1 기능

- [ ] `/web/member/my-memo` 진입 시 기본 탭 = 절 메모, 카드/필터 기존과 동일 동작
- [ ] `?tab=book` / `?tab=chapter` / `?tab=verse` 직접 진입 시 해당 탭 활성화
- [ ] 탭 라벨에 카운트 배지가 표시되며, 비어 있는 탭은 배지 숨김
- [ ] 탭 전환 시 URL `?tab=` 갱신 + 브라우저 뒤로가기로 이전 탭 복원
- [ ] 각 탭에서 번역본 → 책 필터 정상 동작 (탭마다 옵션 분포 다름)
- [ ] 각 탭의 더보기 페이지네이션 독립 동작
- [ ] 책 메모 카드 클릭 → `/web/bible/chapter?...` 로 이동
- [ ] 장 메모 카드 클릭 → `/web/bible/verse?...&chapterNumber=` 로 이동 (verseNumber 없음)
- [ ] 절 메모 카드 클릭 → `/web/bible/verse?...&verseNumber=` 로 이동 (기존 동선 보존)
- [ ] 빈 탭 → 탭별 메시지·CTA 표시
- [ ] 미인증 진입 → `/web/auth/login?returnUrl=/web/member/my-memo?tab=…`
- [ ] 세션 만료 401 → 로그인 리다이렉트 (returnUrl 보존)

### 8.2 회귀

- [ ] 기존 `/api/v1/bibles/my-memos*` 응답 스펙 변동 없음
- [ ] `mypage.html` · `chapter-list.html` · `verse-list.html` 의 단건 메모 CRUD 동작 변동 없음
- [ ] 콘솔 에러 0건 (3개 탭 모두 진입)
- [ ] 브라우저 뒤로 가기로 메모 페이지 → 다른 페이지 이동 후 돌아왔을 때 활성 탭 보존

### 8.3 성능

- [ ] 카운트 프리페치 3개 호출이 병렬로 발사되는지 (Network 탭)
- [ ] 탭 전환 시 동일 탭의 두 번째 진입은 캐시(렌더 결과) 재사용 — 불필요 재호출 없음 (단, 사용자가 새로고침 동작을 한 경우는 제외)

---

## 9. 결정 근거 (Decision Log)

| 결정 | 대안 | 채택 사유 |
|---|---|---|
| 탭 분리 (단일 페이지 내) | 페이지 3개 분리 (`/my-book-memos` 등) | 동일한 필터/페이지네이션 UX 를 공유, 사용자가 한 화면에서 전환 가능 → 작업량 ↓ + 이해비용 ↓ |
| 단일 컨테이너 + 탭별 데이터 갈아끼움 | 탭마다 DOM 패널 분리 | DOM 단순화, 카드/빈상태/더보기 1세트만 유지 — 보수 비용 절감 |
| 탭별 상태 분리(`state.byTab`) | 탭 전환 시 모든 상태 리셋 | 사용자가 탭을 왔다갔다 할 때 페이지·필터를 잃지 않음 — 대량 메모 유저의 UX |
| 카운트 프리페치 = `size=1` 첫 페이지 | 카운트 전용 API 신설 | 기존 슬라이스 응답의 `totalCount` 재사용, 신규 엔드포인트 없음 |
| 책/장 메모 list API 신설 | 단일 통합 `/my-memos?type=` 엔드포인트 | 응답 스키마가 종류별로 다름(verseNumber, chapterNumber 유무) — 타입 안정성과 Swagger 명료성 우선 |
| 카드 빈 상태 CTA 모두 `/web/bible/translation` | 종류별 다른 진입점 | 책/장 메모는 결국 번역본 → 책/장 경로 진입이 필요 — 시작점 통일이 단순 |

---

## 10. 변경이 초래하는 사용자 영향

### 긍정

- 책/장 메모를 페이지 단위로 한눈에 확인 가능 — 기존에는 진입 화면별 산재
- 탭별 카운트로 자기 메모 자산 한눈에 파악
- URL 공유 시 탭까지 보존 (`?tab=chapter` 공유 가능)

### 주의

- 합계 카운트 배지 제거 → 전체 메모 수가 한눈에 안 보임. 필요시 `(책 N + 장 N + 절 N)` 합계를 히어로 작은 회색 텍스트로 부활 가능 (Phase 3)
- `/my-memos/**` SecurityConfig 교정(§4.5)으로 미인증 호출이 현재 NPE/200 → 401 로 바뀜. 정상 브라우저 동선은 영향 없으나, 관찰성 지표상 401 집계가 소폭 증가 가능

---

## 11. Known Issues & Edge Cases

설계 단계에서 **명시적으로 알고 넘어가는 한계점**. Phase 1 범위에서는 수용하고, 필요시 Phase 3 로 보강.

### 11.1 카운트 배지의 stale 가능성

카드 클릭 → 성경 화면에서 메모 생성/삭제 → 브라우저 뒤로가기 → `my-memo.html` 복귀 시, 탭 배지 숫자가 실제 DB 상태와 다를 수 있다.

- **원인**: 최초 진입 시 1회만 프리페치, `popstate` 에서 재호출하지 않음
- **수용 이유**: 새로고침이면 즉시 정합. bfcache 복귀 시 stale 은 흔한 패턴
- **보강안** (Phase 3): `visibilitychange` / `pageshow` 이벤트에서 활성 탭 카운트만 재조회

### 11.2 기본 탭 선택 정책

기본 탭을 "절" 로 고정하면, 절 메모가 0개이고 책/장 메모만 있는 사용자는 빈 상태로 진입한다.

- **대안**: 카운트 프리페치 후 **가장 많은 탭을 기본값** 으로 선택
- **채택 사유**: 기존 `/my-memo` 진입 기대값이 "절 메모 목록" — 기존 사용자의 근육 기억 보존 우선. URL 에 `?tab=` 이 있으면 그 값이 우선

### 11.3 탭 키보드 네비게이션

`role="tablist"` / `role="tab"` 을 붙였지만 WAI-ARIA APG 의 **ArrowLeft/ArrowRight 로 탭 간 이동** 은 구현하지 않음.

- **수용 이유**: 프로젝트 전반이 탭 키보드 내비게이션을 구현하지 않음 (`mypage.html` 과 동일)
- **보강안** (Phase 3): `.my-memo-tabs` 에 키보드 핸들러 추가 (`ArrowLeft`/`Right`/`Home`/`End`), `roving tabindex` 도입

### 11.4 3자릿수 이상 카운트 배지

`.my-memo-tab-badge` 의 `min-width: 1.5rem` 은 2 자릿수까지만 자연스러움. 1000+ 메모 사용자에게 레이아웃 밀림 가능.

- **보강안** (Phase 1 내 포함 가능): `n >= 100 ? "99+" : n` 렌더링

### 11.5 빈 상태 CTA `<a>` 의 초기 href

```html
<a id="myMemoEmptyCta" class="btn btn-primary"></a>
```

`href` 없이 렌더되면 브라우저마다 hover/focus 동작이 다르고, 링크로 인식되지 않을 수 있다.

- **수정**: 기본 `href="/web/bible/translation"` 을 HTML 에 두고, JS 가 탭별 값으로 덮어쓰기 (현재 3 탭 CTA 가 모두 같은 URL 이므로 실질 동등)

### 11.6 `rendered: false` 플래그 활용

`createTabState()` 에 `rendered` 를 두었으나, 본 설계의 `switchTab` 은 매번 새로 fetch 함. 플래그는 **캐시 재사용 정책**이 도입될 때 의미를 가진다.

- **현재 동작**: 탭 전환마다 재조회 — 간단·정합성 우선
- **보강안**: `rendered && !forceRefresh` 이면 기존 DOM 유지 (5.2.3 진입 시퀀스 "이미 렌더된 탭이면 캐시 활용" 문구에 실제 구현을 결합)

### 11.7 필터 상태의 URL 미보존

`?tab=` 만 URL 에 반영하고 `translationId` / `bookOrder` 는 메모리 상태로만 관리. 새로고침 시 필터가 리셋됨.

- **수용 이유**: 단순화 우선, 탭 자체 복원이 핵심 가치
- **보강안** (Phase 3 step 13): `?tab=verse&translationId=1&bookOrder=3` 형태로 확장
