# 나의 성경 메모 전용 화면 분리 설계 문서

## 구현 상태

설계 완료, 미구현.

---

## 1. 요구사항 해석

### R1. 내 메모 탭을 별도 페이지로 분리

현재 `mypage.html` 의 `[계정 설정 | 내 메모]` 2탭 구조에서 **내 메모 탭을 제거**하고, `/web/member/my-memo` 신규 전용 페이지로 이관한다.

- 마이페이지는 "계정 설정" 전용 페이지가 된다 (탭 네비게이션 자체 제거).
- 메모 조회/필터/페이지네이션/빈 상태/스켈레톤 로직은 전용 페이지로 이동.

### R2. 계정 버튼(👤) 드롭다운에 "나의 성경 메모" 메뉴 추가

`fragments/header.html` 의 `#topNavAccountMenu` 에 신규 메뉴 아이템 추가. 클릭 시 `/web/member/my-memo` 로 이동.

- 인증된 사용자에게만 노출 (마이페이지와 동일 패턴).
- 메뉴 순서: **로그인** / **마이페이지** → **나의 성경 메모** (신규) → divider → **로그아웃**

### 비기능 요구사항

- **기존 URL 보존**: `/web/member/mypage?tab=memo` 로의 과거 진입은 **`/web/member/my-memo` 로 301 리다이렉트** 하여 북마크·공유 링크 깨짐 방지.
- **API 재사용**: `/api/v1/bibles/my-memos` 와 `/translations`, `/books` 엔드포인트는 **변경 없이 그대로 사용**. 프런트 분리 작업만 수행.
- **인증**: `MemberWebController` 의 `redirectIfUnauthenticated` 가드 재사용.

---

## 2. 변경 범위

### 신규 파일 — 3개

| 파일                              | 역할                                                          |
|---------------------------------|-------------------------------------------------------------|
| `templates/member/my-memo.html` | 전용 페이지 Thymeleaf 템플릿                                        |
| `static/js/member/my-memo.js`   | 메모 목록/필터/페이지네이션 JS (기존 `mypage.js` 의 memo 섹션 분리)            |
| `static/css/member/my-memo.css` | 메모 전용 CSS (기존 `mypage.css` 의 `.mypage-memo-*` 블록 이관 + 리네이밍) |

### 수정 파일 — 5개

| 파일                                  | 변경 내용                                                                    |
|-------------------------------------|--------------------------------------------------------------------------|
| `kotlin/.../MemberWebController.kt` | `@GetMapping("/my-memo")` 핸들러 추가 + `/mypage?tab=memo` → `/my-memo` 리다이렉트 |
| `templates/member/mypage.html`      | 탭 네비게이션 · `#mypageTabMemo` 패널 제거, 계정 설정만 유지                              |
| `templates/fragments/header.html`   | 드롭다운에 "나의 성경 메모" 메뉴 추가 (`#topNavMyMemoLink`) + 인증 시 `d-none` 해제 로직       |
| `static/js/member/mypage.js`        | 메모 관련 로직(약 200+ 줄) 제거 — `loadMyMemos`, 필터 초기화, 탭 전환 등                    |
| `static/css/member/mypage.css`      | `.mypage-memo-*` 블록 `my-memo.css` 로 이관 후 삭제                              |

### 변경 없는 파일

- `BibleMyMemoApi.kt`, `BibleMemoService.kt` — 기존 API 재사용
- `/api/v1/bibles/my-memos/**` — 변경 없음

---

## 3. 라우팅 & 컨트롤러

### 3.1 신규 라우트

`MemberWebController.kt` 에 추가:

```kotlin
@GetMapping("/my-memo")
fun showMyMemo(authentication: Authentication?): String {
    redirectIfUnauthenticated(authentication, "/web/member/my-memo")?.let { return it }
    return "member/my-memo"
}
```

### 3.2 레거시 URL 리다이렉트

기존 `/web/member/mypage?tab=memo` 진입 처리:

```kotlin
@GetMapping("/mypage")
fun showMyPage(
    authentication: Authentication?,
    @RequestParam(required = false) tab: String?,
): String {
    redirectIfUnauthenticated(authentication, "/web/member/mypage")?.let { return it }
    if (tab == "memo") {
        return "redirect:/web/member/my-memo"
    }
    return "member/mypage"
}
```

### 3.3 SecurityConfig

`SecurityConfig.kt` 에는 `/web/member/**` 매칭 규칙이 **존재하지 않음** (grep 으로 확인). 인증은 **컨트롤러 레벨 `redirectIfUnauthenticated`** 에 일임하는 구조. 신규 `/my-memo` 라우트도 같은 패턴을 사용하므로 SecurityConfig 변경 **불필요**.

---

## 4. 헤더 드롭다운 메뉴 통합

### 4.1 DOM 변경

`fragments/header.html` 의 `#topNavAccountMenu` 내부:

```html
<a id="topNavMyPageLink" href="/web/member/mypage"
   class="top-nav-account-item d-none" role="menuitem">마이페이지</a>

<!-- 신규 -->
<a id="topNavMyMemoLink" href="/web/member/my-memo"
   class="top-nav-account-item d-none" role="menuitem">나의 성경 메모</a>

<div id="topNavAccountDivider" class="top-nav-account-divider d-none" role="separator"></div>

<a id="topNavLogoutLink" ...>로그아웃</a>
```

### 4.2 JS 변경

`fragments/header.html` 의 인증 확인 후 표시 로직에 추가:

```js
const myMemoLink = document.getElementById("topNavMyMemoLink");

checkAuthStatus({
    onAuthenticated: () => {
        logoutLink.classList.remove("d-none");
        myPageLink.classList.remove("d-none");
        myMemoLink.classList.remove("d-none");  // 신규
        accountDivider.classList.remove("d-none");
        // ...
    },
    // ...
});
```

- 키보드 네비게이션(`ArrowDown` 으로 첫 메뉴 아이템 포커스) 은 `.top-nav-account-item:not(.d-none)` 셀렉터라 자동으로 신규 아이템 포함됨.
- 접근성: `role="menuitem"` 유지, 시각 순서 = DOM 순서로 정렬.

---

## 5. 화면 구조 (`my-memo.html`)

### 5.1 배치

```
히어로 (페이지 제목 + 메모 총 개수 배지)
└─ 메인 카드
    ├─ 필터 영역 (번역본 select + 성경 select)
    ├─ 스켈레톤 (초기 로딩)
    ├─ 메모 목록 (.my-memo-list)
    ├─ 빈 상태 (.my-memo-empty) — "아직 작성한 메모가 없습니다" + 성경 읽기 CTA
    └─ 더보기 버튼 (페이지네이션)
```

### 5.2 템플릿 스켈레톤

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="ko">
<head th:replace="~{fragments/head :: head('나의 성경 메모 | ElSeeker', true,
       '/css/hero.css?v=2.2,/css/card.css?v=2.3,/css/member/my-memo.css?v=1.0')}"
      th:with="robotsContent='noindex'"></head>
<body class="has-fixed-nav my-memo-page">
<header th:replace="~{fragments/header :: header}"></header>

<main class="container content-wrapper my-memo-shell">
    <section class="page-hero my-memo-hero">
        <div class="page-hero-content">
            <h1 class="my-memo-title">나의 성경 메모</h1>
            <span id="myMemoCountBadge" class="badge my-memo-count-badge d-none"></span>
        </div>
    </section>

    <article class="card card-panel card-soft my-memo-card border-0 shadow-sm">
        <div class="card-body p-4">
            <div id="myMemoFilter" class="my-memo-filter d-none">
                <select id="myMemoTranslationFilter" class="form-select my-memo-filter-select">
                    <option value="">전체 번역본</option>
                </select>
                <select id="myMemoBookFilter" class="form-select my-memo-filter-select" disabled>
                    <option value="">전체 성경</option>
                </select>
            </div>
            <div id="myMemoSkeleton" class="my-memo-skeleton-group">
                <div class="skeleton skeleton-memo-card"></div>
                <div class="skeleton skeleton-memo-card"></div>
            </div>
            <div id="myMemoList" class="my-memo-list"></div>
            <div id="myMemoEmpty" class="my-memo-empty d-none">
                <p class="mb-2">아직 작성한 메모가 없습니다.</p>
                <a href="/web/bible/translation" class="btn btn-outline-primary btn-sm">
                    성경 읽기에서 메모를 남겨보세요
                </a>
            </div>
            <button id="myMemoMore" class="btn btn-outline-secondary w-100 mt-3 d-none" type="button">
                더보기
            </button>
        </div>
    </article>
</main>

<footer th:replace="~{fragments/footer :: footer}"></footer>

<script type="module" src="/js/member/my-memo.js?v=1.0"></script>
</body>
</html>
```

### 5.3 ID / 클래스 네이밍 정책

| 기존 (mypage 내부)                 | 신규 (my-memo 전용)            |
|--------------------------------|----------------------------|
| `#mypageMemoList`              | `#myMemoList`              |
| `#mypageMemoEmpty`             | `#myMemoEmpty`             |
| `#mypageMemoMore`              | `#myMemoMore`              |
| `#mypageMemoFilter`            | `#myMemoFilter`            |
| `#mypageMemoTranslationFilter` | `#myMemoTranslationFilter` |
| `#mypageMemoBookFilter`        | `#myMemoBookFilter`        |
| `#mypageMemoCountBadge`        | `#myMemoCountBadge`        |
| `#mypageMemoSkeleton`          | `#myMemoSkeleton`          |
| `.mypage-memo-*`               | `.my-memo-*`               |

**이유**: `mypage-` 접두사는 "마이페이지 내부 위젯" 을 시사. 별도 페이지로 이동하므로 `my-memo-` 로 리네이밍하여 소속 변경을 명시.

> 대안 검토: `.mypage-memo-*` 클래스를 `.memo-list-*` 등 범용 이름으로 변경하는 것도 가능하나, 현재 공유 범위가 단일 페이지이므로 `my-memo-` 로 한정하는 편이 스코프 관리에 유리.

---

## 6. JS 동작 (`my-memo.js`)

### 6.1 임포트 & 상태

현재 `mypage.js` 의 memo 관련 코드는 **여러 지점에 분산**되어 있다 (961줄 중):

- 71행: `memoSkeleton` DOM 참조
- 83–87행: 상태 변수 (`memoPage`, `memoHasNext`, `memoLoading`, `memoTranslationFilter`, `memoBookFilter`)
- 89–94행: memo DOM 참조 (list, empty, moreBtn, filter selects)
- 96–155행: 탭 전환 로직 (`switchTab`, `getInitialTab`, 탭 click 리스너 — **my-memo 에서는 불필요, mypage 에서도 제거**)
- 534행: `formatMemoDate`
- 549행: `createMemoCard`
- 577–700행: `loadMyMemos` 본체 (fetch + render + pagination)

이 블록들을 `my-memo.js` 로 이관 + ID 리네이밍:

```js
import {fetchWithAuthRetry} from "/js/common-util.js?v=2.2";

const state = {
    page: 0,
    hasNext: false,
    loading: false,
    translationFilter: null,
    bookFilter: null,
};

const elements = {
    skeleton: document.getElementById("myMemoSkeleton"),
    list: document.getElementById("myMemoList"),
    empty: document.getElementById("myMemoEmpty"),
    more: document.getElementById("myMemoMore"),
    filter: document.getElementById("myMemoFilter"),
    translationSelect: document.getElementById("myMemoTranslationFilter"),
    bookSelect: document.getElementById("myMemoBookFilter"),
    countBadge: document.getElementById("myMemoCountBadge"),
};
```

### 6.2 핵심 함수

- `loadMyMemos(append: boolean)` — 기존 로직 재사용 (`fetchMemoList` API URL, 에러 핸들링, 페이지네이션)
- `loadTranslations()` — 최초 1회 `/api/v1/bibles/my-memos/translations` 조회, `<select>` 옵션 구성
- `loadBooks(translationId)` — 번역본 선택 시 `/api/v1/bibles/my-memos/books?translationId=...` 조회
- `createMemoCard(memo)` — 링크에 `from=my-memo` 쿼리 추가하여 구절 상세에서 "뒤로" 시 `/web/member/my-memo` 로 복귀 가능하게 처리
- `resetAndReload()` — 필터 변경 시 page 초기화 + 재조회

### 6.3 인증 처리

- 페이지 진입 시 서버 `redirectIfUnauthenticated` 로 1차 차단
- 세션 만료 등 런타임 401 발생 시 `redirectToLogin()` (기존 `mypage.js` 패턴) — `buildLoginRedirectUrl("/web/member/my-memo")` 로 returnUrl 설정

### 6.4 URL → 구절 상세 이동 시 복귀 처리

```js
card.href = `/web/bible/verse?translationId=${...}&bookOrder=${...}&chapterNumber=${...}&verseNumber=${...}&from=my-memo`;
```

> **현재 상태 확인**: grep 결과 `verse-list.js` 및 다른 JS 어디에도 `from=mypage` 분기 로직이 **존재하지 않음**. 즉 현재 `from=mypage` 쿼리는 단순 URL 마커일 뿐 복귀 동작에 영향을 주지 않는다.
>
> **Phase 2 제안**: `verse-list.js` 에 `from` 쿼리 파라미터 해석 로직을 신설하여 `from=my-memo` 일 때 top-nav back 버튼이 `/web/member/my-memo` 로 복귀하도록 한다. Phase 1 범위에서는 쿼리만 전달하고 동작 추가는 보류.

---

## 7. CSS 전략

### 7.1 `my-memo.css` 로 이관할 블록

`mypage.css` 에서 다음 블록을 `my-memo.css` 로 **이동** (원본은 삭제):

- `.mypage-memo-header`
- `.mypage-memo-list`
- `.mypage-memo-card` (카드 아이템 · hover · focus · date)
- `.mypage-memo-empty`
- `.mypage-memo-filter` / `.mypage-memo-filter-select`
- `.mypage-badge-count` (히어로 배지)
- `.skeleton.skeleton-memo-card`
- 관련 반응형 미디어쿼리

### 7.2 `mypage.css` 에서 **완전 삭제**할 블록 (my-memo.css 로 이관 안 함, 그냥 제거)

탭 UI 자체가 없어지므로 다음은 어디에도 필요 없음:

- `.mypage-tabs-wrapper`
- `.mypage-tabs`
- `.mypage-tab` / `.mypage-tab.active`
- `.mypage-tab-badge`
- `.mypage-tab-panel`
- 탭 sticky 관련 규칙 (top-nav 연동 높이 계산 등)

### 7.3 리네이밍

전역 치환: `.mypage-memo-` → `.my-memo-`, `.mypage-badge-count` → `.my-memo-count-badge` (sed 또는 에디터 replace).

### 7.4 공통 재사용

`.page-hero`, `.card-panel`, `.card-soft`, `.skeleton` 기본 블록은 **공유 스타일**이므로 변경 없이 사용. `hero.css`, `card.css` 는 두 페이지 공통으로 head 에 로드됨.

---

## 8. API (변경 없음, 재사용 확인)

| 엔드포인트                                                       | 메서드 | 용도                    |
|-------------------------------------------------------------|-----|-----------------------|
| `/api/v1/bibles/my-memos?page&size&translationId&bookOrder` | GET | 내 메모 목록 (페이지네이션 + 필터) |
| `/api/v1/bibles/my-memos/translations`                      | GET | 필터 드롭다운용 번역본 목록       |
| `/api/v1/bibles/my-memos/books?translationId`               | GET | 번역본별 성경 목록            |

모두 `@AuthenticationPrincipal JwtPrincipal` 주입된 인증 엔드포인트. 기존 `BibleMyMemoApi.kt` 그대로 사용.

---

## 9. 기존 `mypage.html` 정리

### 9.1 제거 대상

- `.mypage-tabs-wrapper` + `<nav class="mypage-tabs">` 블록 전체
- `#mypageTabSettings` 의 `role="tabpanel"` 속성 → 단순 `<div>` 로 전환 (탭 구조 해제)
- `#mypageTabMemo` 패널 전체
- `<article class="mypage-memo-*">` 관련 DOM

### 9.2 `mypage.js` 정리

**제거 대상** (라인 번호는 현재 기준):

- **71행**: `memoSkeleton` DOM 참조
- **83–87행**: 상태 변수 `memoPage` / `memoHasNext` / `memoLoading` / `memoTranslationFilter` / `memoBookFilter`
- **89–94행**: memo DOM 참조 (`memoList`, `memoEmpty`, `memoMoreBtn`, `memoFilterContainer`, `memoTranslationSelect`, `memoBookSelect`)
- **96–101행**: `tabButtons`, `tabPanels` (settings + memo 맵), `tabScrollPositions`
- **103–140행**: `switchTab` 함수 전체
- **142–149행**: `getInitialTab` 함수 전체
- **151–155행**: 탭 버튼 click 리스너 설정
- **534–575행**: `formatMemoDate`, `createMemoCard` 함수
- **577–700행**: `loadMyMemos` 본체
- **기타**: `#mypageTabMemoBadge` 관련 업데이트 로직 전부

**정리 후 `mypage.js` 에 남는 책임**: 프로필 로딩 + 닉네임 편집 + OAuth 연동 관리 + 토스트 + Danger Zone 링크만. 탭 · 메모 관련 코드는 **한 줄도 남지 않아야** 한다.

> 초기 진입 시 `?tab=settings` 쿼리 처리도 불필요 (탭 자체가 없음). URL 정리 로직(`window.history.replaceState` 로 `?tab` 설정) 도 같이 제거.

### 9.3 마이페이지 히어로의 메모 수 배지

현재 히어로의 탭 배지(`mypageTabMemoBadge`)는 단일 페이지에서만 유의미. 마이페이지에서는 제거. 메모 개수는 `/web/member/my-memo` 페이지의 `#myMemoCountBadge` 에서만 노출.

---

## 10. 구현 순서

### Phase 1 — 핵심 기능 (이 문서 범위)

1. **백엔드**: `MemberWebController.showMyMemo()` 추가 + 레거시 `?tab=memo` 리다이렉트
2. **템플릿**: `templates/member/my-memo.html` 신규 작성
3. **JS**: `static/js/member/my-memo.js` 신규 작성 (mypage.js memo 섹션 이관 + ID 리네이밍)
4. **CSS**: `static/css/member/my-memo.css` 신규 작성 (mypage.css 의 `.mypage-memo-*` 이관 + `.my-memo-*` 리네이밍)
5. **헤더**: `fragments/header.html` 에 `#topNavMyMemoLink` 추가 + `checkAuthStatus` 에 표시 로직 추가
6. **정리**: `mypage.html` 에서 탭/메모 영역 제거, `mypage.js` 에서 memo 로직 제거, `mypage.css` 에서 이관된 블록 제거
7. **캐시 버전**:
    - `mypage.css?v=4.1` → `?v=4.2`
    - `mypage.js?v=4.0` → `?v=4.1`
    - 신규: `my-memo.css?v=1.0`, `my-memo.js?v=1.0`

### Phase 2 — 회귀 방지 / UX 보강

1. `verse-list.js` 에서 `?from=my-memo` 처리 → "뒤로" 클릭 시 `/web/member/my-memo` 로 복귀
2. 컨트롤러 리다이렉트 301 상태 코드 명시 (현재는 Spring 기본 302). 영구 이동이므로 301 더 정확:
   ```kotlin
   @GetMapping("/mypage")
   fun showMyPage(
       @RequestParam(required = false) tab: String?,
       response: HttpServletResponse,
   ): String {
       if (tab == "memo") {
           response.status = HttpStatus.MOVED_PERMANENTLY.value()
           response.setHeader("Location", "/web/member/my-memo")
           return ""
       }
       // ...
   }
   ```
3. 마이페이지 히어로에 "나의 성경 메모" 바로가기 카드/링크 추가 (선택)

### 수동 검증 체크리스트

**기능 검증**

- [ ] 로그인 후 `/web/member/mypage` → 계정 설정만 표시 (탭 없음)
- [ ] 드롭다운 `👤` → 로그인 전: 로그인 / 로그인 후: 마이페이지 + 나의 성경 메모 + 로그아웃
- [ ] `/web/member/my-memo` 직접 진입 시 메모 목록 + 필터 정상 동작
- [ ] 미인증 사용자 진입 시 `/web/auth/login?returnUrl=...` 로 리다이렉트
- [ ] `/web/member/mypage?tab=memo` → `/web/member/my-memo` 로 자동 이동
- [ ] 번역본 필터 선택 → 성경 필터 활성화 → 해당 필터로 목록 재조회
- [ ] "더보기" 페이지네이션 정상 동작
- [ ] 빈 상태(메모 0개) "성경 읽기에서 메모를 남겨보세요" CTA 노출
- [ ] 세션 만료 시 401 → 로그인 페이지로 리다이렉트 (returnUrl 보존)

**정리 검증 (잔존물 0 확인)**

- [ ] `mypage.html` 에 `mypage-tab*` 클래스/ID 잔존 없음 (grep 확인)
- [ ] `mypage.js` 에 `memo`, `switchTab`, `tabButtons`, `tabPanels`, `tabScrollPositions` 잔존 없음 (grep 확인)
- [ ] `mypage.css` 에 `.mypage-tab*`, `.mypage-memo-*`, `.mypage-badge-count` 잔존 없음 (grep 확인)
- [ ] 브라우저 DevTools Console 에러 0건 (마이페이지 · 나의 성경 메모 양쪽)

---

## 11. 변경이 초래하는 사용자 영향

### 긍정적 영향

- 메모 조회가 메인 내비게이션(`👤` 메뉴)에서 **1-클릭으로 접근 가능** → 기존 2-클릭(마이페이지 → 내 메모 탭) 대비 단축
- URL `/web/member/my-memo` 가 기능을 그대로 표현 → 공유·북마크 친화적
- 페이지 전용화로 향후 메모 편집·검색·태그 등 확장 여지 확보

### 주의점

- 기존 `/web/member/mypage?tab=memo` 북마크/링크 → 리다이렉트로 커버
- 마이페이지 내에서 메모로 빠르게 전환하던 동선이 사라짐 → 마이페이지 히어로에 "나의 성경 메모" 바로가기 링크 추가 권장 (Phase 2)
