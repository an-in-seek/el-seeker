# 홈 화면 인기 검색어 노출 설계

## 개요

`templates/index.html` 에 **성경 구절 인기 검색어 TOP5** 와 **성경 사전 인기 검색어 TOP5** 두 랭킹을 각각 분리된 카드 형태로 노출한다.

- 두 랭킹 집계·저장·공개 API 설계는 이미 다음 문서에 정의되어 있다. 본 문서는 **홈 화면 노출** 범위만 다룬다.
  - [bible/search-keyword-ranking-design.md](../bible/search-keyword-ranking-design.md) — 성경 구절 검색 키워드 집계
  - [study/dictionary-search-keyword-ranking-design.md](../study/dictionary-search-keyword-ranking-design.md) — 성경 사전 검색 키워드 집계

## 구현 상태

- 사전 공개 랭킹 API: 구현 완료 (`DictionarySearchKeywordApi`)
- 성경 구절 공개 랭킹 API: **미구현** (관리자 API `AdminBibleSearchKeywordApi` 만 존재)
- 홈 노출 UI: 미구현

## 1. 요구사항 해석

| # | 요구사항 | 해석 |
|---|---|---|
| R1 | 2개 랭킹을 **분리해서** 노출 | 동일 페이지 내 별도 `<section>` 2개. DOM·CSS·JS 모두 독립. 한쪽 실패가 다른 쪽 노출을 막지 않는다. |
| R2 | 검색 횟수 기준 TOP5 | 각 API 를 `limit=5` 로 호출. 정렬은 서버(`search_count DESC, last_searched_at DESC`) 에 위임. |
| R3 | 비로그인 포함 모든 사용자 노출 | 두 API 모두 `permitAll`. 홈은 비로그인 기본 진입점이므로 토큰 헤더 불필요 → `fetchWithAuthRetry` 가 아닌 일반 `fetch` 사용. |
| R4 | 랭킹 클릭 시 검색 진입 (권장) | 구절 랭킹 → `/web/bible/search?keyword={k}`. 사전 랭킹 → `/web/study/dictionary?keyword={k}` (웹 경로는 단수, API 는 복수 `dictionaries`). URL 엔코딩 필수. |
| R5 | 랭킹 비어 있음 / 실패 허용 | 결과가 0건이거나 API 가 실패하면 해당 섹션만 숨긴다. 홈의 hero/카드/universe 영역에 영향 없음. |

### 비기능 요구사항

- **초기 렌더 지연 금지**: 랭킹 fetch 는 hero/menu 그리드 초기 페인팅 이후 수행. (`DOMContentLoaded` 이후 비동기)
- **캐시 정책**: 각 서비스의 `@Cacheable` (TTL 30s) 에 의존. 홈 측 추가 캐시는 불필요.
- **SEO**: 랭킹은 사용자 행위 기반 변동성 높은 데이터 → SSR 불필요, CSR 로 충분.

## 2. API 설계

### 2.1 기존 API (그대로 사용)

**성경 사전 인기 검색어 — 기존**

```
GET /api/v1/study/dictionaries/search-keywords/ranking?limit=5
```

응답 스펙: `DictionarySearchKeywordRankingResponse`

```json
{
  "items": [
    { "rank": 1, "keyword": "바울", "searchCount": 248 }
  ],
  "refreshedAt": "2026-04-19T10:15:30Z"
}
```

### 2.2 신규 구현 필요 API

**성경 구절 인기 검색어 — 공개 엔드포인트 추가**

```
GET /api/v1/bibles/search-keywords/ranking?limit=5
```

- 기존 `AdminBibleSearchKeywordApi` 와 **URL 분리**: 관리자용은 `/api/v1/admin/bible/search-keywords/**`, 공개용은 `/api/v1/bibles/search-keywords/**`.
- 서비스 레이어는 기존 `BibleSearchKeywordService.getRanking(limit)` 를 그대로 재사용 (Caffeine 캐시 키도 공유).
- 권한: `permitAll` (`/api/v1/bibles/**` prefix 는 이미 `SecurityConfig` 에서 `permitAll` 처리되어 있어 추가 설정 불필요).
- 응답 스펙: 사전 랭킹과 동일한 구조로 통일하여 홈 JS 가 단일 렌더러로 처리한다.

**구현 체크리스트**

1. `BibleSearchKeywordApi` (`/api/v1/bibles/search-keywords`) + `BibleSearchKeywordApiDocument` 신규 작성
   - ⚠️ 공개 API 이므로 **`@AuthenticationPrincipal JwtPrincipal` 파라미터를 주입하지 말 것**. `permitAll` 경로에서 비로그인 요청은 `AnonymousAuthenticationToken` 의 principal 이 문자열 `"anonymousUser"` 이므로 `JwtPrincipal` 타입으로는 **`null` 이 주입**되고, 컨트롤러에서 null 체크를 빠뜨리면 NPE 가 발생한다. (Admin API 와 차별점)
2. `BibleSearchKeywordRankingResponse` 신규 DTO 작성
   - ⚠️ 기존 `AdminSearchKeywordRankingResponse` 를 **재사용하지 말 것**. 현재는 동일 스키마이지만 관리자 전용 필드(예: blocked 상태, 원본/정규화 키워드 병기) 가 추후 추가될 수 있으므로 클라이언트 응답은 독립 스키마로 분리한다.
3. `BibleSearchKeywordService.getRanking` — 변경 없음, 재사용. `@Cacheable(key="#limit")` 이므로 Admin API 가 먼저 호출해 캐시를 채우면 공개 API 도 동일 엔트리를 공유한다.
4. Swagger 그룹은 기존 bible client API 와 동일 그룹에 편입.
5. 보안 설정: 별도 조치 불필요. `SecurityConfig.kt:79-87` 에 나열된 `/api/v1/bibles/translations/**/memos|highlights|state|chapter-memo|book-memo` 는 `authenticated()` 로 먼저 매칭되지만, **신규 `/api/v1/bibles/search-keywords/**` 는 해당 목록에 포함되지 않으므로 `/api/v1/bibles/**` permitAll (line 99) 로 정상 매칭된다.** 새 경로를 memo/highlight 목록에 추가하지 말 것.

### 2.3 공통 응답 규약 (홈 렌더러 기준)

| 필드 | 타입 | 설명 |
|---|---|---|
| `items[].rank` | int | 1-based 순위 |
| `items[].keyword` | string | 표시용 키워드 (최근 원본) |
| `items[].searchCount` | long | 누적 검색 횟수 (표기 선택) |
| `refreshedAt` | ISO-8601 | 응답 생성 시각 (표기 선택) |

- 홈 TOP5 카드에서는 `rank` + `keyword` 만 필수 표시. `searchCount` 는 디자인에 따라 뱃지로 함께 노출하거나 생략.
- `items` 가 비어 있으면 해당 섹션 전체를 숨긴다 (노이즈 방지).

### 2.4 호출 실패 처리

| 상황 | 처리 |
|---|---|
| HTTP 4xx/5xx | `console.warn` + 해당 섹션 `hidden` 처리 |
| `items.length === 0` | 해당 섹션 `hidden` 처리 |
| 네트워크 타임아웃 | 5초 `AbortController` → `hidden` 처리 |
| 두 API 동시 실패 | 양쪽 섹션 모두 hidden. 홈 기본 영역은 그대로 노출 |

## 3. 화면 노출 흐름

### 3.1 배치

`index.html` 의 섹션 순서:

```
home-hero
home-menu-grid
home-popular-search   ← 신규: 구절 랭킹 + 사전 랭킹 (한 row, 2 column)
universe-section
```

데스크톱에서는 두 카드가 한 줄에 나란히, 모바일(`<768px`) 에서는 세로로 쌓인다 (Bootstrap 그리드 `col-12 col-md-6`).

### 3.2 템플릿 스켈레톤

```html
<section class="home-popular-search" aria-label="인기 검색어">
  <div class="row g-3">
    <div class="col-12 col-md-6">
      <article class="popular-search-card"
               data-keyword-ranking="bible"
               data-link-template="/web/bible/search?keyword={kw}"
               data-aria-template="순위 {rank}위, {keyword} 구절 검색"
               hidden>
        <header class="popular-search-card-header">
          <h3 class="popular-search-title">성경 구절 인기 검색어</h3>
          <a class="popular-search-more" href="/web/bible/search">더보기</a>
        </header>
        <ol class="popular-search-list" data-keyword-list></ol>
      </article>
    </div>
    <div class="col-12 col-md-6">
      <article class="popular-search-card"
               data-keyword-ranking="dictionary"
               data-link-template="/web/study/dictionary?keyword={kw}"
               data-aria-template="순위 {rank}위, {keyword} 사전 검색"
               hidden>
        <header class="popular-search-card-header">
          <h3 class="popular-search-title">성경 사전 인기 검색어</h3>
          <a class="popular-search-more" href="/web/study/dictionary">더보기</a>
        </header>
        <ol class="popular-search-list" data-keyword-list></ol>
      </article>
    </div>
  </div>
</section>
```

- 초기 상태는 `hidden`. 데이터 도착 시 렌더 후 `hidden` 제거 → **CLS(레이아웃 시프트) 최소화**.
- `data-link-template` 로 랭킹 종류별 이동 경로만 바꿔 단일 JS 렌더러로 통합.
- `data-aria-template` 로 각 링크의 `aria-label` 을 서버 템플릿에서 결정 → 다국어/문구 수정 용이.
- `<ol>` 사용 시 브라우저가 접근성 트리에 순위를 포함. `rank` 뱃지는 CSS 로 별도 강조.
- 사전 웹 경로는 `/web/study/dictionary` (단수) 임에 유의 — `DictionaryWebController` 매핑 기준.

### 3.3 JS 플로우

신규 파일: `src/main/resources/static/js/popular-search.js`

```js
const ENDPOINTS = {
    bible:      "/api/v1/bibles/search-keywords/ranking?limit=5",
    dictionary: "/api/v1/study/dictionaries/search-keywords/ranking?limit=5",
};

const TIMEOUT_MS = 5000;

export const initPopularSearch = () => {
    document.querySelectorAll("[data-keyword-ranking]").forEach(renderCard);
};

const renderCard = async (card) => {
    const type = card.dataset.keywordRanking;
    const endpoint = ENDPOINTS[type];
    if (!endpoint) return;

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);

    try {
        const res = await fetch(endpoint, { signal: controller.signal, credentials: "omit" });
        if (!res.ok) return;
        const data = await res.json();
        const items = Array.isArray(data?.items) ? data.items : [];
        if (items.length === 0) return;
        paint(card, items);
        card.hidden = false;
    } catch (e) {
        // network error / timeout → keep hidden
    } finally {
        clearTimeout(timer);
    }
};

const paint = (card, items) => {
    const list = card.querySelector("[data-keyword-list]");
    const linkTpl = card.dataset.linkTemplate;
    const ariaTpl = card.dataset.ariaTemplate;
    list.replaceChildren(...items.map((it) => {
        const li = document.createElement("li");
        li.className = "popular-search-item";

        const a = document.createElement("a");
        a.className = "popular-search-link";
        a.href = linkTpl.replace("{kw}", encodeURIComponent(it.keyword));
        if (ariaTpl) {
            a.setAttribute(
                "aria-label",
                ariaTpl.replace("{rank}", it.rank).replace("{keyword}", it.keyword),
            );
        }

        const rank = document.createElement("span");
        rank.className = "popular-search-rank";
        rank.textContent = String(it.rank);

        const keyword = document.createElement("span");
        keyword.className = "popular-search-keyword";
        keyword.textContent = it.keyword;

        a.append(rank, keyword);
        li.appendChild(a);
        return li;
    }));
};
```

- `credentials: "omit"` 로 쿠키 미전송 → CDN 캐시 친화적이며 랭킹은 사용자별 개인화가 없음.
- `keyword` / `rank` 모두 **`textContent` 로만 주입**하여 XSS 차단. `innerHTML` 사용 금지.
- `encodeURIComponent` 로 한글·특수문자 안전하게 URL 에 전달.
- `aria-label` 은 `data-aria-template` 기반으로 스크린리더 친화 (예: "순위 1위, 사랑 구절 검색").

기존 `src/main/resources/static/js/index.js` 상단 static import 로 추가 (현재 `universe-bg.js` 로딩 방식과 동일):

```js
// index.js — 상단 imports 에 추가
import {initPopularSearch} from "/js/popular-search.js?v=1.0";

// 기존 DOMContentLoaded 핸들러 안에서 호출
document.addEventListener("DOMContentLoaded", () => {
    initHeroCarousel();
    initUniverse("universeCanvas", "universeSection");
    initPopularSearch();
    // ... 이하 기존 로직
});
```

> 동적 `import()` 대신 static import 를 사용하는 이유: `index.js` 의 다른 모듈(`universe-bg.js`, `storage-util.js`) 이 모두 static import 로 로드되어 있어 일관성 유지. 또한 hero 페인팅은 DOMContentLoaded 시점에 이미 완료되므로 랭킹 스크립트 로드가 hero 렌더를 지연시키지 않는다.

### 3.4 CSS 개요

**권장 위치**: 기존 `src/main/resources/static/css/home.css` 에 섹션 추가.

- 홈 전용 스타일은 현재 `home.css` 한 파일로 관리되므로 (`home-hero`, `home-menu-grid`, `universe-section`) 동일 파일에 이어서 선언하는 것이 홈 스타일 응집도 유지에 유리하다.
- 버전은 `home.css?v=3.7` → `?v=3.8` 로 bump. `index.html` head `extraCss` 문자열에서만 수정.

스타일 클래스:

- `.home-popular-search` — 섹션 wrapper. 상하 여백은 `home-menu-grid` · `universe-section` 간격 토큰과 일치시킬 것.
- `.popular-search-card` — 카드 컨테이너. 기존 `card.css` 의 공용 shadow/radius 토큰 재사용.
- `.popular-search-list` — `padding: 0; list-style: none;`
- `.popular-search-rank` — 원형 뱃지. TOP3(`rank<=3`) 는 강조 색상 (데이터 속성 `data-rank` 나 렌더 시 `.top-rank-{n}` 클래스 부여 방식 중 택일).
- `.popular-search-link` — hover 는 반드시 `@media (hover: hover) and (pointer: fine)` 블록 안에서만 선언 (CLAUDE.md 규칙).
- 모바일: `<768px` 에서 카드 세로 스택, 행 간격 축소, 글자 크기 0.95rem.

> 별도 파일(`/css/home-popular-search.css`) 로 분리하는 것도 허용되며 프로젝트 전반의 기능별 CSS 분리 관례(`book-search.css`, `search.css` 등) 와 일치한다. 다만 홈 초기 진입 시 CSS 요청 수 증가와 `home.css` 와의 스타일 겹침 가능성을 고려할 때, **`home.css` 확장이 1안, 별도 파일은 차선**으로 둔다.

### 3.5 사용자 인터랙션 시퀀스

```
[브라우저]
 ├─ index.html 파싱
 ├─ hero/menu 최초 페인트 (home-popular-search 는 hidden)
 ├─ index.js static import 로 popular-search.js 로드
 ├─ DOMContentLoaded
 │    └─ initPopularSearch() 호출 → 카드별 fetch 시작
 ├─ fetch GET /api/v1/bibles/search-keywords/ranking?limit=5        (5초 타임아웃)
 ├─ fetch GET /api/v1/study/dictionaries/search-keywords/ranking?limit=5   (병렬)
 ├─ 각 응답 도착 → items 유무 판정 → 렌더 or hidden 유지
 └─ 사용자 카드 클릭
      └─ /web/bible/search?keyword=... 또는 /web/study/dictionary?keyword=... 로 이동
```

두 fetch 는 서로 독립적이며, 한쪽 지연·실패가 다른 쪽 노출을 막지 않는다 (R5).

## 4. 구현 순서

1. **백엔드**:
   - `BibleSearchKeywordApi` (공개, `/api/v1/bibles/search-keywords`) + `BibleSearchKeywordApiDocument` + `BibleSearchKeywordRankingResponse` DTO 신규 작성.
   - `@AuthenticationPrincipal` 미주입 확인, Admin DTO 재사용 금지 확인.
   - `BibleSearchKeywordService.getRanking` 재사용 (Caffeine 캐시 공유).
   - Swagger 에서 `/api/v1/bibles/search-keywords/ranking?limit=5` 호출 성공 확인.
2. **프런트 JS**: `src/main/resources/static/js/popular-search.js` 신규 작성.
3. **프런트 CSS**: `src/main/resources/static/css/home.css` 에 §3.4 클래스 추가.
4. **템플릿**:
   - `index.html` 에 §3.2 섹션을 `home-menu-grid` 와 `universe-section` 사이에 삽입.
   - `index.js` 상단에 `popular-search.js` static import 추가 후 DOMContentLoaded 핸들러에서 `initPopularSearch()` 호출.
5. **캐시 버전 버스팅**:
   - `home.css?v=3.7` → `?v=3.8` (index.html `extraCss` 문자열)
   - `index.js?v=2.8` → `?v=2.9`
   - 신규 `popular-search.js?v=1.0`
6. **수동 검증**:
   - 비로그인 상태로 홈 진입 시 두 카드가 모두 노출되는지
   - 한쪽 API 를 강제 404/500 으로 바꿔도 나머지 카드는 정상 노출되는지
   - `items` 가 빈 배열일 때 해당 섹션이 `hidden` 으로 유지되는지
   - 키워드 클릭 시 `/web/bible/search?keyword=...`, `/web/study/dictionary?keyword=...` 로 이동하는지 (한글·공백·특수문자 포함 케이스 포함)
   - 모바일 폭(`<768px`) 에서 카드가 세로로 스택되는지
   - 스크린리더로 "순위 1위, 사랑 구절 검색" 형태 aria-label 이 읽히는지
