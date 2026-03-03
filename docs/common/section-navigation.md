# ElSeeker UI/UX 설계 문서: 섹션 네비게이션 (Bottom Tab Bar + Navigation Rail)

## 1. 개요

ElSeeker 서비스의 주요 화면에 화면 크기별 섹션 네비게이션을 도입하여, 사용자가 어느 화면에 있더라도 핵심 섹션으로 즉시 이동할 수 있도록 탐색 접근성을 강화합니다.

Material Design 3 가이드라인에 따라 화면 크기별로 최적의 네비게이션 패턴을 적용합니다.

| 화면 크기 | 네비게이션 패턴 | 비고 |
|---|---|---|
| Compact (< 600px, 모바일) | **Bottom Tab Bar** | 하단 고정 탭 |
| Medium (600~991px, 태블릿) | **Navigation Rail** | 좌측 고정 세로 바 |
| Expanded (≥ 992px, 데스크톱) | **기존 상단 GNB 유지** | 별도 섹션 네비 없음 |

본 문서는 현재 프로젝트의 실제 구조(Thymeleaf SSR + 상단 고정 내비게이션 + 인증 흐름)를 반영한 설계안입니다.

> **용어 정리:** 기존 상단 내비게이션을 "GNB(Global Navigation Bar)", 모바일 하단 고정 탭을 "Bottom Tab Bar", 태블릿 좌측 세로 바를 "Navigation Rail"로 구분합니다.

## 2. 메뉴 구조 및 순서

Bottom Tab Bar와 Navigation Rail은 동일한 5개 메뉴를 공유합니다.

| 순서 | 메뉴명 | 현재 프로젝트 경로 | 아이콘 | 설명 |
|---|---|---|---|---|
| 1 | **성경** | `/web/bible/translation` | SVG (책 아이콘) | 성경 읽기, 구절 탐색, 번역본 전환 |
| 2 | **학습** | `/web/study` | SVG (연필 아이콘) | 사전/역사/개요 영상/가계도 학습 |
| 3 | **홈** | `/` | SVG (홈 아이콘) | 메인 대시보드 |
| 4 | **게임** | `/web/game` | SVG (게임패드 아이콘) | 성경 퀴즈/타자/OX/퍼즐 |
| 5 | **커뮤니티** | `/web/community` | SVG (말풍선 아이콘) | 게시글 조회/소통 |

- Bottom Tab Bar: 좌→우 순서 배치, 홈은 중앙(3번째)
- Navigation Rail: 위→아래 순서 배치, 홈은 최상단(1번째)
- 아이콘은 SVG로 통일하며, `aria-hidden="true"` 처리 후 별도 텍스트 레이블과 조합
- 비활성 상태는 outline 스타일, 활성 상태는 filled 스타일로 구분

## 3. UI/UX 상세 요구사항

### 3.1 Bottom Tab Bar (모바일, < 600px)

- **위치 고정:** `position: fixed; bottom: 0;` 화면 스크롤과 무관하게 하단 고정
- **높이:** 56px (아이콘 24px + 텍스트 12px + 상하 패딩)
- **안전 영역 대응:** `padding-bottom: env(safe-area-inset-bottom);`
- **터치 영역:** 각 버튼 최소 44x44px

### 3.2 Navigation Rail (태블릿, 600~991px)

- **위치 고정:** `position: fixed; left: 0; top: var(--top-nav-height);` 상단 GNB 아래부터 좌측 전체 높이
- **너비:** 80px (Material Design 3 표준)
- **레이아웃:** 아이콘(24px) + 텍스트 레이블(12px) 세로 정렬, 항목 간 간격 12px
- **터치 영역:** 각 버튼 최소 44x44px

### 3.3 공통 상태 표현

Bottom Tab Bar와 Navigation Rail에 동일하게 적용합니다.

- **Active:** Primary Color 강조 + filled 아이콘 + `aria-current="page"`
- **Inactive:** 회색(`#9E9E9E`) + outline 아이콘
- **터치 피드백:** 탭 시 `scale(0.95)` + `opacity: 0.7` 전환 (150ms)
- **상태 전환:** `transition: color 200ms ease, transform 150ms ease;`
- `prefers-reduced-motion: reduce` 시 모든 애니메이션 비활성화

### 3.4 반응형 브레이크포인트

| 조건 | Bottom Tab Bar | Navigation Rail | 상단 GNB |
|---|---|---|---|
| `max-width: 599.98px` (모바일) | **표시** | 숨김 | 유지 |
| `600px ~ 991.98px` (태블릿) | 숨김 | **표시** | 유지 |
| `min-width: 992px` (데스크톱) | 숨김 | 숨김 | 유지 |

> **설계 근거:** Material Design 3의 Compact(Bottom Bar) → Medium(Navigation Rail) → Expanded(Navigation Drawer) 3단 분기를 따릅니다. Expanded 구간에서 Navigation Drawer 대신 기존 상단 GNB를 유지하는 것은, 이미 데스크톱에서 상단 GNB가 섹션 전환 역할을 수행하고 있어 별도 Drawer 도입이 불필요하기 때문입니다.

## 4. 기술적 고려사항

### 4.1 시맨틱 마크업 및 접근성(a11y)

Bottom Tab Bar와 Navigation Rail은 단일 `<nav>` fragment로 구현하며, CSS로 표현 형태만 전환합니다.

```html
<nav class="section-nav" aria-label="섹션 탐색 메뉴">
  <a href="..." class="section-nav-item active" aria-current="page">
    <svg aria-hidden="true">...</svg>
    <span class="section-nav-label">홈</span>
  </a>
  ...
</nav>
```

- 기존 상단 GNB가 `aria-label="Primary Navigation"`이므로, 섹션 네비는 `aria-label="섹션 탐색 메뉴"`로 구분
- 키보드: Tab키로 항목 간 이동, Enter/Space로 활성화
- Navigation Rail에서는 상하 화살표키로 항목 간 이동 지원

### 4.2 z-index 계층

기존 프로젝트 z-index 계층에 맞춰 배치합니다.

| 요소 | z-index |
|---|---|
| Section Nav — Bottom Tab Bar / Navigation Rail (신규) | **1010** |
| 기존 `fixed-bottom-nav` (페이지별 하단 네비) | 1000 |
| 상단 GNB `.top-nav-row` | 1030 |
| 계정 메뉴 `.top-nav-account-menu` | 1040 |
| 구절 FAB `.verse-fab` | 1050 |
| Toast `.app-toast-container` | 1060 |

### 4.3 라우팅 동기화 (Active 상태 매핑)

Bottom Tab Bar와 Navigation Rail 모두 동일한 매핑 로직을 공유합니다. URL Pathname prefix로 활성 항목을 결정하며, **긴 prefix부터 우선 매칭**합니다.

```
매칭 우선순위 (위에서 아래로 평가):
1. /web/bible     → 성경
2. /web/study     → 학습
3. /web/game      → 게임
4. /web/community → 커뮤니티
5. /              → 홈 (exact match only)
6. 그 외          → 활성 항목 없음 (모두 비활성)
```

- `/web/community/write`는 커뮤니티 활성
- `/web/member/mypage`, `/web/auth/login` 등 5개 카테고리 외 경로에서는 **어떤 항목도 활성화하지 않음**
- 홈(`/`)은 `pathname === "/"` exact match로 처리 (prefix 매칭 시 모든 경로에 해당하는 문제 방지)

### 4.4 터치 환경 대응

- hover 스타일은 `@media (hover: hover) and (pointer: fine)` 내에서만 적용
- `-webkit-tap-highlight-color: transparent;` 설정 후 커스텀 터치 피드백 적용
- iOS Safari overscroll 대응: Bottom Tab Bar 영역에 `overscroll-behavior: none;` 적용

### 4.5 SSR 페이지 전환 시 깜빡임

Thymeleaf SSR은 전체 페이지 리로드가 발생하므로, 네비게이션이 순간적으로 사라졌다 다시 나타날 수 있습니다. 항목 클릭 시 즉시 Active 상태를 전환(`click` 이벤트에서 CSS 클래스 변경)하여 체감 응답 속도를 높입니다.

## 5. 현재 프로젝트 반영 보완사항

### 5.1 기존 상단 GNB와의 역할 분리

| 역할 | 상단 GNB | Bottom Tab Bar / Navigation Rail |
|---|---|---|
| 섹션 전환 (성경/학습/게임 등) | - | O |
| 뒤로가기 | O (`#topNavBackButton`) | - |
| 계정 (로그인/마이페이지) | O | - |
| 검색 | O | - |

### 5.2 기존 페이지별 하단 네비(`fixed-bottom-nav`)와의 공존

`verse-list.html`, `chapter-list.html`, `book-description.html`에 이미 `position: fixed; bottom: 0; z-index: 1000`인 페이지별 하단 네비(이전/다음 장·책 이동, 높이 ~68px)가 존재합니다.

**모바일(Bottom Tab Bar) 공존 전략: 스크롤 방향 기반 auto-hide**

이 페이지들에서 글로벌 Bottom Tab Bar를 전면 숨김하면 섹션 전환 접근성을 잃게 됩니다. 대신 스크롤 방향에 따라 글로벌 탭을 자동으로 숨기고 노출하여, 읽기 몰입과 섹션 탐색을 모두 지원합니다.

| 스크롤 방향 | Bottom Tab Bar | 페이지별 네비 (`fixed-bottom-nav`) |
|---|---|---|
| **아래로** (콘텐츠 소비 중) | `translateY(100%)` 숨김 | `bottom: 0`으로 하강 |
| **위로** (탐색 의도) | `translateY(0)` 노출 | `bottom: var(--bottom-tab-height)`로 상승 |

구현 상세:

```css
/* CSS 변수: :root에 정의하여 전역 참조 가능 */
:root {
  --top-nav-height: 52px; /* 상단 GNB 실제 높이: 버튼 44px + 패딩 8px */
  --bottom-tab-height: calc(56px + env(safe-area-inset-bottom, 0px));
  --nav-rail-width: 80px;
}

/* Bottom Tab Bar: 스크롤 auto-hide (모바일 + 공존 페이지 전용) */
@media (max-width: 599.98px) {
  body.has-dual-bottom-nav .section-nav {
    transition: transform 300ms ease;
    will-change: transform;
  }
  body.has-dual-bottom-nav.bottom-tab-hidden .section-nav {
    transform: translateY(100%);
  }
}

/* 페이지별 네비: 글로벌 탭 노출 시 위로 밀림 (모바일 + 공존 페이지 전용) */
@media (max-width: 599.98px) {
  .has-dual-bottom-nav .fixed-bottom-nav {
    transition: bottom 300ms ease;
    bottom: var(--bottom-tab-height);
  }
  .has-dual-bottom-nav.bottom-tab-hidden .fixed-bottom-nav {
    bottom: 0;
  }
}
```

```js
// common-nav.js (스크롤 auto-hide 로직 — 모바일 공존 페이지 전용)
if (document.body.classList.contains('has-dual-bottom-nav')) {
  let lastScrollY = window.scrollY;
  const SCROLL_THRESHOLD = 10; // 최소 스크롤 거리 (jitter 방지)

  window.addEventListener('scroll', () => {
    const delta = window.scrollY - lastScrollY;
    if (Math.abs(delta) < SCROLL_THRESHOLD) return;

    if (delta > 0 && window.scrollY > 0) {
      document.body.classList.add('bottom-tab-hidden');    // 스크롤 다운 → 숨김
    } else {
      document.body.classList.remove('bottom-tab-hidden'); // 스크롤 업 → 노출
    }
    lastScrollY = window.scrollY;
  }, { passive: true });
}
```

주의사항:
- `transform: translateY()`를 사용하여 GPU 가속 애니메이션 적용 (`bottom` 속성 변경 대비 렌더링 성능 우수)
- `passive: true`로 스크롤 이벤트 등록하여 메인 스레드 블로킹 방지
- `SCROLL_THRESHOLD`(10px)로 미세 스크롤 시 떨림(jitter) 방지
- `prefers-reduced-motion: reduce` 시 `transition: none` 적용
- 페이지 최상단(`scrollY === 0`)에서는 항상 노출 상태 유지

**태블릿(Navigation Rail) 공존: 수평 겹침 보정**

Navigation Rail은 좌측 고정(80px)이고 `fixed-bottom-nav`는 `left: 0; width: 100%`이므로, 태블릿에서 좌측 80px이 수평으로 겹칩니다. 태블릿 미디어 쿼리에서 `fixed-bottom-nav`의 위치를 보정합니다.

```css
@media (min-width: 600px) and (max-width: 991.98px) {
  .fixed-bottom-nav {
    left: var(--nav-rail-width);
    width: calc(100% - var(--nav-rail-width));
  }
}
```

### 5.3 content-wrapper 여백 보강

화면 크기별 네비게이션 위치에 따라 본문 영역 여백을 조정합니다.

```css
/* 모바일: 하단 여백 */
@media (max-width: 599.98px) {
  .content-wrapper {
    padding-bottom: var(--bottom-tab-height);
  }
  /* 공존 페이지: Bottom Tab Bar + 페이지별 네비 높이 합산 */
  body.has-dual-bottom-nav .content-wrapper {
    padding-bottom: calc(var(--bottom-tab-height) + 68px);
  }
  /* 글로벌 탭 숨김 시: 페이지별 네비 높이만 유지 */
  body.has-dual-bottom-nav.bottom-tab-hidden .content-wrapper {
    padding-bottom: 68px;
  }
}

/* 태블릿: 좌측 여백 */
@media (min-width: 600px) and (max-width: 991.98px) {
  .content-wrapper {
    margin-left: var(--nav-rail-width);
  }
}
```

- 모바일 공존 페이지(`has-dual-bottom-nav`)는 auto-hide 시 CSS만으로 여백 자동 조정
- 태블릿은 Navigation Rail 너비(80px)만큼 좌측 여백 적용
- 푸터(`footer.html`)가 있는 페이지도 네비에 의해 잘리지 않도록 동일한 여백 적용

### 5.4 적용/제외 페이지 매트릭스

| 페이지 분류 | 모바일 (Bottom Tab Bar) | 태블릿 (Navigation Rail) | 비고 |
|---|---|---|---|
| 홈, 학습, 게임 목록, 커뮤니티 목록 | **표시** | **표시** | 주요 랜딩 페이지 |
| 성경 번역본 선택, 책 목록 | **표시** | **표시** | |
| 성경 구절 읽기, 장 목록, 책 설명 | **스크롤 auto-hide** | **표시** | 모바일만 `fixed-bottom-nav` 공존 처리 (5.2절 참조) |
| 커뮤니티 상세, 글쓰기 | **표시** | **표시** | |
| 개별 게임 페이지 (퀴즈, 타자 등) | **숨김** | **숨김** | 게임 몰입 UX 보장 |
| 로그인 페이지 | **숨김** | **숨김** | 독립 레이아웃 (`has-fixed-nav` 미사용) |
| 관리자 페이지 (`/web/admin/**`) | **숨김** | **숨김** | 별도 레이아웃 |
| 법적 페이지 (이용약관, 개인정보) | **숨김** | **숨김** | 독립 레이아웃 |
| 오류 페이지 (404/500) | **숨김** | **숨김** | Spring Boot 기본 오류 페이지, fragment 포함 불가 |
| 회원 탈퇴 완료 | **숨김** | **숨김** | 탈퇴 후 네비게이션 불필요 |

### 5.5 Thymeleaf fragment 포함 전략

Bottom Tab Bar와 Navigation Rail을 단일 fragment로 구현하고, CSS 미디어 쿼리로 표현 형태를 전환합니다.

기존 `hideHomeButton` 패턴(`header.html`의 `th:if="${hideHomeButton != true}"`)과 동일한 방식을 사용합니다.

```html
<!-- 각 템플릿에서 (단일 fragment가 화면 크기에 따라 Bottom Tab / Rail로 전환) -->
<div th:replace="~{fragments/section-nav :: section-nav}"></div>

<!-- 제외 대상 페이지에서는 포함하지 않음 -->
```

- `has-fixed-nav` 클래스를 사용하는 페이지에만 fragment를 포함
- 제외 대상 페이지(로그인, 관리자, 법적 페이지 등)에서는 fragment 자체를 포함하지 않음

### 5.6 인증/권한 반영 규칙

섹션 네비 메뉴 진입은 다음 보안 정책을 따릅니다.

- 공개 진입 가능:
  - `/`, `/web/bible/translation`, `/web/study`, `/web/community`, `/web/game`
- 인증 필수:
  - `/web/game/**` (개별 게임 페이지, `/web/game` 제외)
  - `/web/community/write` (글쓰기 인증 필요)

비로그인 사용자가 인증 필요 메뉴를 선택한 경우 `/web/auth/login?returnUrl=...`로 이동시키는 기존 흐름을 재사용합니다.

### 5.7 코딩 규칙 반영 체크

- Hover 스타일은 데스크톱 한정: `@media (hover: hover) and (pointer: fine)`
- CSS/JS 수정 시 해당 HTML의 `?v=` 파라미터 버전 증가 필수
- 전역 내비게이션 변경 시 기존 `auth-check.js` 기반 인증 상태 확인 로직과 충돌 여부 확인
- 신규 CSS 색상은 CSS 변수(`--section-nav-bg`, `--section-nav-active`, `--section-nav-inactive`)로 정의

### 5.8 템플릿/리소스 적용 위치

- Fragment: `src/main/resources/templates/fragments/section-nav.html` (신규)
- CSS: `src/main/resources/static/css/section-nav.css` (신규)
- JS: `src/main/resources/static/js/common-nav.js` (신규)

## 6. 단계별 도입 제안

1. `section-nav` fragment/CSS/JS를 추가하고 모바일 Bottom Tab Bar를 홈/성경(번역본·책 목록)/학습/커뮤니티 페이지에 우선 적용
2. 게임 목록 페이지 적용 (개별 게임 페이지는 제외)
3. 페이지별 하단 네비 존재 페이지(verse-list, chapter-list, book-description)에 스크롤 auto-hide 공존 처리 적용
4. 모바일 실기기(iOS safe-area·overscroll, Android gesture navigation)에서 UI 간섭 검증
5. 태블릿 Navigation Rail CSS 추가 및 좌측 여백(`margin-left`) 적용
6. 태블릿 실기기(iPad 세로/가로 전환, Android 태블릿)에서 Rail ↔ Tab Bar 전환 검증
7. 데스크톱(`min-width: 992px`)에서 섹션 네비 비노출 최종 확인

## 7. 향후 고려사항

- **뱃지/알림:** 커뮤니티 새 글 수, 게임 진행률 등 뱃지 표시 (기존 상단 GNB 뱃지 패턴 참조)
- **전체 페이지 스크롤 auto-hide 확대:** 현재는 `fixed-bottom-nav` 공존 페이지에만 적용하나, 향후 커뮤니티 상세 등 긴 콘텐츠 페이지로 확대 검토
- **성경 탭 복귀 위치:** 현재는 항상 번역본 선택(`/web/bible/translation`)으로 이동하나, 향후 마지막 읽던 위치 복원 검토
