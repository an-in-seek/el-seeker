# ElSeeker UI/UX 설계 문서: Dark Theme (메인 화면 적용)

## 구현 상태: 📝 설계 단계 (미구현)

## 1. 개요

ElSeeker 메인 화면(`index.html`)에 다크 테마를 도입하여, 야간/저조도 환경에서의 가독성과 눈 피로도를 개선하고 사용자가 선호하는 시각 경험을 선택할 수 있도록 합니다.

본 문서는 1차 적용 범위를 `index.html`로 한정하되, 향후 전체 페이지 확장을 염두에 둔 토큰 기반 아키텍처로 설계합니다.

| 항목 | 결정 |
|---|---|
| 1차 적용 범위 | `index.html` (히어로, 메뉴 카드, 인기 검색어, 우주 섹션, 푸터, GNB, 섹션 네비) |
| 토큰 전략 | CSS Custom Properties + `data-theme` 속성 (`<html>` 적용) |
| Bootstrap 5.3+ 통합 | `data-bs-theme` 동시 설정 (현 프로젝트 5.3.0 확정 — `build.gradle.kts`) |
| 사용자 선호 감지 | `prefers-color-scheme` 자동 감지 + 수동 override |
| 영속화 | `localStorage` 키 `themePreference` (값: `light` \| `dark` \| 미설정=시스템 따름) |
| FOUC 방지 | `<head>` 동기 인라인 스크립트로 첫 렌더 전 속성 부여 |
| 토글 UI 위치 | 1차: 상단 GNB 계정 메뉴 내 첫 번째 항목 (모바일/데스크톱 공통) |

## 2. 사용자 시나리오

| ID | 시나리오 | 기대 동작 |
|---|---|---|
| US-1 | 사용자가 처음 방문 (`themePreference` 미설정) | 시스템 `prefers-color-scheme` 자동 감지 → 라이트/다크 적용 |
| US-2 | 토글 클릭 (Light → Dark) | `themePreference='dark'` 저장, `<html data-theme="dark" data-bs-theme="dark">` 즉시 반영 |
| US-3 | 같은 브라우저에서 재방문 | `themePreference` 값 우선 적용 (시스템 변경 무시) |
| US-4 | "시스템 따름" 옵션 선택 | `themePreference` 키 삭제 → OS 설정에 따라 자동 변동 |
| US-5 | 페이지 새로고침 | FOUC 없이 즉시 적용된 테마로 첫 paint |
| US-6 | 시스템 따름 모드 중 OS 다크 모드 토글 | `matchMedia('change')` 리스너로 자동 갱신 |
| US-7 | 1차 범위 외 페이지 이동 | 토큰화 안 된 컴포넌트는 라이트로 보임. 토글 버튼 자체는 노출되며 동작은 하지만 시각 변화 없음 |

## 3. 기술 아키텍처

### 3.1 토큰 시스템 (CSS Custom Properties)

색상은 항상 시맨틱 토큰을 통해 참조하며, 컴포넌트는 raw hex 값을 직접 사용하지 않습니다.

```css
/* :root 에 라이트 기본값, html[data-theme="dark"] 에 다크 오버라이드
   다크 셀렉터를 element+attribute(0,0,1,1)로 specificity 한 단계 올림 */
:root {
    --color-bg-base: #ffffff;
    --color-bg-elevated: #f8f9fa;
    --color-bg-overlay: rgba(255, 255, 255, 0.92);
    --color-text-primary: #212529;
    --color-text-secondary: #6c757d;
    --color-text-muted: #adb5bd;       /* WCAG 미달 — 의도된 시각 약화 (placeholder/disabled) */
    --color-border: #dee2e6;            /* 카드/구분선 — 장식적 */
    --color-border-control: #ced4da;    /* form input 보더 — 3:1 이상 보장 (UI 1.4.11) */
    --color-accent: #0d6efd;
    --color-accent-fg: #ffffff;
    --color-shadow: rgba(0, 0, 0, 0.08);
    --hero-overlay-from: rgba(0, 0, 0, 0.35);
    --hero-overlay-to: rgba(0, 0, 0, 0.55);
}

html[data-theme="dark"] {
    --color-bg-base: #0f1216;
    --color-bg-elevated: #181c22;
    --color-bg-overlay: rgba(15, 18, 22, 0.92);
    --color-text-primary: #e6e8eb;
    --color-text-secondary: #adb5bd;
    --color-text-muted: #868e96;       /* 다크 배경에서 #6c757d 는 3:1 미달 → 한 단계 밝게 */
    --color-border: #2a3038;            /* 카드/구분선 — 장식적 */
    --color-border-control: #495057;    /* form input 보더 — 3:1 이상 */
    --color-accent: #4dabf7;
    --color-accent-fg: #0f1216;
    --color-shadow: rgba(0, 0, 0, 0.5);
    --hero-overlay-from: rgba(0, 0, 0, 0.55);
    --hero-overlay-to: rgba(0, 0, 0, 0.75);
}
```

**중요:** 토큰을 `:root` 에만 정의하고 컴포넌트 CSS 가 토큰을 사용하지 않는 한 그 컴포넌트 색은 변하지 않습니다. 따라서 1차 범위 외 페이지는 자동으로 안전(라이트 유지)합니다.

#### 3.1.1 기존 `:root` 변수와의 통합 정책

`section-nav.css:4-13` 이 이미 `:root` 에 자체 변수(`--section-nav-bg`, `--section-nav-active`, `--section-nav-inactive`, `--section-nav-indicator`, `--section-nav-border` 등)를 정의하고 있습니다. 다음 정책으로 충돌을 회피합니다:

- 기존 컴포넌트 변수는 **유지** 하되, 그 값을 시맨틱 토큰으로 **재정의**
  ```css
  /* section-nav.css 변경안 */
  :root {
      --section-nav-bg: var(--color-bg-base);
      --section-nav-active: var(--color-accent);
      --section-nav-inactive: var(--color-text-muted);
      --section-nav-border: var(--color-border);
      /* ... */
  }
  ```
- 이렇게 하면 컴포넌트 셀렉터 변경 없이 시맨틱 토큰의 모드 전환을 자동 상속
- 향후 다른 페이지에서도 동일 패턴 적용

#### 3.1.2 CSS 로드 순서 / Specificity

`:root` 와 `[data-theme="dark"]` 는 specificity 가 동일합니다(둘 다 0,0,1,0). 따라서 **CSS 로드 순서가 결정** 합니다.

- `theme.css` 는 `useCommonCss` 자동 로드 4종(`common`, `top-nav`, `footer`, `section-nav`) **보다 먼저** 로드 — 이렇게 하면 컴포넌트 CSS 의 `:root` 자체 변수 정의가 후행이라 우선되지만, 컴포넌트 변수가 시맨틱 토큰을 `var()` 로 참조하므로 모드 전환은 정상 동작
- `theme.css` 안에서도 라이트 `:root` → 다크 `[data-theme="dark"]` 순서로 작성
- 더 안전한 방법: 다크 셀렉터를 `html[data-theme="dark"]` 로 specificity 한 단계 올려 로드 순서 의존성 제거

### 3.2 모드 표현 — `<html data-theme>` + `data-bs-theme` 동시 설정

```html
<html data-theme="dark" data-bs-theme="dark">
```

- `data-theme`: 프로젝트 자체 토큰 분기에 사용
- `data-bs-theme`: Bootstrap 5.3+ 가 자체 다크 색상 팔레트(`.bg-light`, `.text-muted` 등 utility) 를 자동 분기

두 속성을 항상 동기화해 설정합니다. 한쪽만 설정하면 Bootstrap utility 클래스를 쓴 영역이 라이트로 남아 어색해집니다.

> **버전 확정:** `build.gradle.kts` 의 `org.webjars:bootstrap:5.3.0` 으로 5.3.0 확정. `data-bs-theme` 자동 분기가 동작합니다. 영향 utility 클래스 예: `text-muted`, `text-body`, `text-secondary`, `bg-light`, `bg-white`, `bg-body`, `border`, `text-bg-*`, form 컨트롤 기본 색상.

#### 3.2.1 Bootstrap 자체 다크 팔레트와 프로젝트 토큰 일관화

Bootstrap 5.3 의 `data-bs-theme="dark"` 는 자체 색상 팔레트를 사용하므로 프로젝트의 시맨틱 토큰과 미묘하게 다를 수 있습니다. 일관성을 위해 `theme.css` 안에서 Bootstrap 의 핵심 변수를 프로젝트 토큰으로 매핑합니다.

```css
html[data-theme="dark"] {
    /* 프로젝트 시맨틱 토큰 */
    --color-bg-base: #0f1216;
    /* ... */

    /* Bootstrap 변수 동기화 */
    --bs-body-bg: var(--color-bg-base);
    --bs-body-color: var(--color-text-primary);
    --bs-border-color: var(--color-border);
    --bs-secondary-color: var(--color-text-secondary);
    --bs-tertiary-bg: var(--color-bg-elevated);
    --bs-link-color: var(--color-accent);
    --bs-link-hover-color: var(--color-accent);
}
```

### 3.3 모드 결정 로직

`localStorage.themePreference` 값을 우선 참조하고, 없으면 시스템 설정을 따릅니다. **`data-theme` 속성 값으로는 항상 `'light'` 또는 `'dark'` 두 값 중 하나만 사용** 하고, "시스템 따름" 상태는 `localStorage` 키 부재로만 표현합니다.

```js
function resolveTheme() {
    const stored = localStorage.getItem('themePreference');
    if (stored === 'light' || stored === 'dark') return stored;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}
```

#### 3.3.1 `matchMedia` `change` 이벤트 처리 정책

- **`themePreference` 미설정(시스템 따름) 인 경우만** OS 변경을 즉시 반영
- 명시적 선택(`'light'` / `'dark'`) 상태에서는 `change` 이벤트가 발생해도 무시 (사용자 선택 우선)
- 토글로 "시스템 따름" 으로 전환할 때 `localStorage` 키를 삭제하고 `change` 리스너를 다시 활성화

```js
mediaQuery.addEventListener('change', (e) => {
    if (localStorage.getItem('themePreference') !== null) return; // 명시 선택 보호
    applyTheme(e.matches ? 'dark' : 'light');
});
```

### 3.4 FOUC 방지 — `<head>` 동기 인라인 스크립트

라이트 → 다크 깜빡임은 사용자 경험을 크게 해칩니다. `head.html` 의 가능한 한 빠른 위치(스타일시트 링크 직전)에 동기 인라인 스크립트로 속성을 적용합니다.

```html
<head>
    <meta charset="UTF-8">
    <!-- ... 다른 메타 ... -->
    <script>
        (function () {
            var theme;
            try {
                var stored = localStorage.getItem('themePreference');
                theme = (stored === 'light' || stored === 'dark') ? stored : null;
            } catch (e) { /* localStorage 차단 환경 */ }
            if (!theme) {
                try {
                    theme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
                } catch (e) {
                    theme = 'light';
                }
            }
            var html = document.documentElement;
            html.setAttribute('data-theme', theme);
            html.setAttribute('data-bs-theme', theme);
        })();
    </script>
    <link rel="stylesheet" href="...">
</head>
```

- **반드시 동기 인라인** — `defer`/`module` 사용 금지
- localStorage 와 matchMedia 둘 다 try/catch 로 감싸 비정상 환경에서도 라이트로 안전 폴백
- 외부 모듈로 분리하지 않는 이유: 첫 paint 전 실행 보장 + 한 줄 네트워크 비용 절약

## 4. UI/UX 상세

### 4.1 테마 토글 UI

- **위치:** `top-nav-account-menu` 내 상단 영역 (계정 항목들 위에 separator 로 구분되는 "테마" 서브섹션). 다음 3개 항목 노출:
  - "라이트 모드" / "다크 모드" / "시스템 따름"
  - "시스템 따름" 항목에는 현재 적용 모드를 보조 텍스트로 표기 — 예: `시스템 따름 (현재 다크)` — 사용자가 어떤 색이 적용될지 인지 가능하게
  - 현재 선택 항목은 좌측 체크 아이콘 + `aria-checked="true"`
- **마크업:** **각 항목은 `<a>` 가 아니라 `<button type="button" role="menuitemradio">`** — 페이지 이동이 아닌 상태 토글이므로 (기존 메뉴의 로그인/마이페이지 링크는 `<a>` 유지)
- **모바일/데스크톱 공통** — 좁은 화면에서 `top-nav-right` 의 추가 버튼은 폭이 부족
- **계정 메뉴는 비로그인 상태에서도 표시** 되므로 모든 사용자가 토글 가능
- **메뉴 자동 닫힘:** 토글 클릭 시 메뉴는 자동으로 닫힘 (기존 `closeAccountMenu()` 활용)
- **접근성:** `role="group"` 으로 라디오 그룹 묶기, `aria-checked` 로 단일 선택 상태 명시, 키보드 `↑↓` 이동
- **터치 영역:** 메뉴 항목 최소 44px 높이

> **대안:** 사용 빈도가 충분히 높다고 판단되면 `top-nav-right` 의 search 버튼 좌측에 단일 토글 버튼으로 노출 (3-state 순환). 1차에서는 메뉴 항목 방식이 화면 점유 최소.

### 4.2 전환 애니메이션

- 배경/텍스트 색상은 200ms 페이드 (`transition: background-color 200ms ease, color 200ms ease, border-color 200ms ease`)
- 단, **첫 페이지 로드 직후 트랜지션은 비활성** 해야 시스템 따름 변동 시 깜빡임 방지
  - `<html class="theme-transitions-disabled">` 로 시작 → `requestAnimationFrame` 두 번 후 클래스 제거
  - 이 클래스는 `*, *::before, *::after { transition: none !important; }` 정의
- `prefers-reduced-motion: reduce` 사용자에게는 트랜지션 0ms 강제

### 4.3 색상 대비 (WCAG)

| 레이어 | 라이트 페어 | 다크 페어 | 적용 기준 | 실제 대비 (예상) |
|---|---|---|---|---|
| 본문 텍스트 vs 배경 | `#212529` / `#ffffff` | `#e6e8eb` / `#0f1216` | AA 4.5:1 | 라이트 16.1 / 다크 13.5 |
| 보조 텍스트 vs 배경 | `#6c757d` / `#ffffff` | `#adb5bd` / `#0f1216` | AA 4.5:1 | 라이트 4.65 / 다크 7.5 |
| 액센트(링크/버튼 텍스트) vs 배경 | `#0d6efd` / `#ffffff` | `#4dabf7` / `#0f1216` | AA 4.5:1 | 라이트 4.6 / 다크 7.4 |
| Form 컨트롤 보더 vs 배경 | `#ced4da` / `#ffffff` | `#495057` / `#0f1216` | AA 1.4.11 (3:1) | 별도 토큰 `--color-border-control` 검토 |
| 카드/구분선 보더 vs 배경 | `#dee2e6` / `#ffffff` | `#2a3038` / `#0f1216` | **WCAG 적용 대상 아님** (장식적 시각 구분) | 1.27 / 1.6 — 의도된 약한 분리 |

> 카드 보더는 인터랙티브 UI 요소가 아니므로 WCAG 1.4.11 의 3:1 대상이 아닙니다. 대신 form input 등 사용자가 입력 영역을 인지해야 하는 보더는 **별도 토큰** (`--color-border-control`) 으로 분리해 3:1 이상 보장합니다.

## 5. index.html 컴포넌트별 적용 매트릭스

| 컴포넌트 | 라이트 | 다크 | 비고 |
|---|---|---|---|
| `body` 배경 | `var(--color-bg-base)` | 동일 토큰 | `common.css` |
| `.home-hero` 슬라이드 이미지 | 그대로 사용 | 동일 (이미지 X) | 오버레이만 어둡게 |
| `.home-hero-overlay` | linear-gradient 약함 | 더 진한 그라데이션 | `--hero-overlay-*` |
| `.home-hero-title` | 흰색 (이미지 위) | 흰색 유지 | 변경 없음 |
| `.home-hero-cta` | 액센트 토큰 | 액센트 토큰 | |
| `.home-menu-card` | `--color-bg-elevated` | 동일 | 보더/그림자 토큰화 |
| `.home-menu-card .text-muted` | `#6c757d` | `--color-text-secondary` | Bootstrap utility — `data-bs-theme` 자동 분기로 해결 (5.3+) |
| `.popular-search-card` | 토큰 | 토큰 | 순위 번호 대비 확보 |
| `.universe-section` | 다크 그대로 유지 | 변경 없음 | 라이트 모드에서도 다크 컨셉 유지 — 라이트 본문과의 경계용 페이드 그라데이션 추가 |
| 상단 GNB (`top-nav.css`) | 흰색 | `--color-bg-overlay` | sticky 시 가독 유지 |
| 푸터 (`footer.css`) | 라이트 | 토큰 | useCommonCss 자동 로드 → 1차 토큰화 필수 |
| 섹션 네비 (`section-nav.css`) | 라이트 | 토큰 | useCommonCss 자동 로드 → 1차 토큰화 필수 |

### 5.1 우주 섹션 특이사항
이미 다크 컨셉이라 `data-theme="light"` 에서도 그대로 다크 유지. 라이트 모드에서는 위쪽 그라데이션을 추가해 `body` 흰색과의 경계가 자연스럽도록 처리합니다.

### 5.2 이미지 자산 정책
- 히어로 슬라이드(`thebible1.png`, `thebible2.png`): 양 모드 동일 사용. 오버레이로 톤 보정.
- 메뉴 카드 이모지(📖 📚 🎮 💬): 그대로. OS 렌더 의존이라 양 모드 자연스러움.
- SVG 아이콘은 `fill="currentColor"` 적용 권장.
- OG 이미지(`elseeker_og.png`): 양 모드 공통 1장 유지 (분기 시 SEO 복잡성 증가).

## 6. 영속화 정책

`storage-util.js` 의 `STORAGE_KEYS` 상수에 `THEME_PREFERENCE: "themePreference"` 를 추가하고, 동일 모듈에 `ThemeStore` 헬퍼를 신설합니다.

| 키 | 값 | 의미 |
|---|---|---|
| `themePreference` | `'light'` | 명시적 라이트 선택 |
| `themePreference` | `'dark'` | 명시적 다크 선택 |
| (키 부재) | — | 시스템 따름 |

- `'system'` 같은 값을 명시적으로 저장하지 않습니다 — 키 삭제로 표현하면 매 방문마다 OS 설정 기준으로 자동 평가됩니다
- `localStorage` 차단 환경에서는 매 페이지 로드마다 시스템 감지 결과 적용 (영속화 불가, 기능적 문제는 없음)
- 사용자 인증과 무관 (브라우저 단위)
- 향후 서버 동기화 필요 시 `member` 도메인의 `preferences` 엔드포인트로 확장

## 7. 접근성 / 사용자 보호

| 항목 | 대응 |
|---|---|
| WCAG AA 대비 | 모든 시맨틱 토큰 페어 4.5:1 이상 (UI 요소 3:1) |
| 색맹 | 색만으로 정보 구분 안 함 (액센트 + 아이콘/텍스트 병행) |
| `prefers-reduced-motion` | 테마 전환 트랜지션 0ms |
| `prefers-color-scheme` | 미설정 사용자에게 OS 설정 자동 반영 |
| 키보드 | 메뉴 항목 `Enter`/`Space`/`↑↓` 동작, focus ring 양 테마 모두 가시 |
| 스크린리더 | `role="menuitemradio"` + `aria-checked` 로 현재 선택 안내 |
| 인쇄 | `@media print` 에서 라이트 강제 |

## 8. 구현 단계

### Phase 1 — 토큰 인프라
- `static/css/theme.css` 신규 — `:root` 라이트 + `html[data-theme="dark"]` 다크 토큰 정의 (specificity 한 단계 올려 로드 순서 의존성 제거)
- `fragments/head.html` 수정:
  - `<head>` 안 가능한 한 위쪽에 FOUC 방지 동기 인라인 스크립트 삽입
  - **`theme.css` 는 `useCommonCss` 와 무관하게 항상 로드** — `useCommonCss=false` 인 페이지(admin, login 등)에서도 토글 동작 일관성 확보를 위해 `bootstrap.min.css` 직후 `<link rel="stylesheet">` 무조건 추가
  - 인쇄 강제: `theme.css` 안에 `@media print { :root, html[data-theme="dark"] { ...라이트 토큰 강제... } }` 정의 (`:root` 만 override 하면 다크 모드일 때 후행 셀렉터가 우선되어 인쇄가 다크로 나갈 위험)
- `static/js/storage-util.js` 의 `STORAGE_KEYS` 에 `THEME_PREFERENCE` 추가, `ThemeStore` 헬퍼 export

### Phase 2 — 글로벌 컴포넌트 토큰화 (`useCommonCss=true` 자동 로드 4종)
- `common.css` — `body` 등 글로벌 셀렉터의 색상을 토큰으로
- `top-nav.css` — GNB 배경/텍스트/보더 토큰화
- `footer.css` — 푸터 토큰화
- `section-nav.css` — 모바일 하단 탭 / 태블릿 좌측 레일 토큰화
- 캐시 버스팅 `?v=` 갱신

### Phase 3 — index.html 전용 컴포넌트 토큰화
- `home.css`, `card.css`, `popular-search.css`, `hero.css` 토큰 치환
- `templates/index.html` 캐시 버스팅 갱신

### Phase 4 — 토글 UI
- `fragments/header.html` 의 `topNavAccountMenu` 안에 3개 항목(라이트/다크/시스템) 추가
- `static/js/theme-toggle.js` 신규 작성 — 클릭 처리, `localStorage` 동기화, `data-theme`+`data-bs-theme` 동시 갱신, `matchMedia` `change` 리스너
- `header.html` 의 인라인 모듈 스크립트에서 import 또는 별도 `<script type="module">` 로 로드

### Phase 5 — 검증
- 라이트/다크/시스템 3 상태 전환 동작
- 새로고침 시 FOUC 없음 (느린 네트워크 환경 포함)
- WCAG AA 대비 자동 검증 (axe DevTools 권장)
- 모바일/태블릿/데스크톱 시각 회귀 없음
- 1차 범위 외 페이지(`bible`, `study`, `game`, `community`, `member` 등) 라이트 정상 유지 검증
- 로그인/로그아웃 흐름에서 테마 유지

### Phase 6 — 향후 확장 (별도 PR)
- 다른 도메인 페이지 컴포넌트 점진적 토큰화
- `theme-color` 메타 태그 분기로 모바일 브라우저 chrome 색상 일치
- 사용자 계정 동기화 (선택)

## 9. 트레이드오프 / 의사결정

| 결정 | 채택 | 대안 | 이유 |
|---|---|---|---|
| 토큰 표현 | CSS Custom Properties | Sass 변수 | 런타임 전환 필요 |
| 모드 표현 | `data-theme` + `data-bs-theme` 속성 | 클래스 (`.theme-dark`) | Bootstrap 통합 + 단일값 시맨틱 |
| 적용 단위 | `<html>` (`:root`) | `<body>` | `:root` 토큰이 모든 자식 요소에서 상속됨 (현재 프로젝트는 iframe/embed 미사용이라 추가 고려 불필요) |
| 1차 범위 | `index.html` + 전역 4종(`common/top-nav/footer/section-nav`) | 전체 한 번에 | 회귀 위험 최소화, 점진 확장. 토큰을 안 쓴 컴포넌트는 자동 라이트 유지 |
| FOUC 방지 | `<head>` 동기 인라인 스크립트 | CSS 만으로 처리 | 사용자 명시 선택을 첫 paint 에 반영하려면 JS 필수 |
| 토글 UI | 계정 메뉴 내 3 항목 | 단일 버튼 / 설정 페이지 | 화면 점유 최소 + 모바일/데스크톱 공통 위치 |
| `'system'` 영속화 | 키 삭제로 표현 | `'system'` 값 저장 | 매 방문마다 OS 설정 자동 평가 (단순) |
| 토글 노출 범위 | 1차에 모든 페이지 노출 | index 한정 노출 | 1차 범위 외에서도 글로벌 4종(common/top-nav/footer/section-nav)이 토큰화돼 시각 효과가 일부 적용됨 — 노출해도 어색하지 않음 |
| `theme.css` 로드 위치 | `useCommonCss` 와 무관하게 항상 로드 | `useCommonCss=true` 일 때만 | admin/login 등에서도 토글 일관 동작 보장 |
| 다크 셀렉터 specificity | `html[data-theme="dark"]` (0,0,1,1) | `[data-theme="dark"]` (0,0,1,0) | 다른 `:root` 정의와의 로드 순서 의존성 제거 |
| 인쇄 시 강제 라이트 | `:root, html[data-theme="dark"]` 양쪽 override | `:root` 만 override | `[data-theme="dark"]` 토큰이 후행이라 `:root` 만으로는 다크 출력 위험 |

## 10. 향후 검토

- **Skeleton / Spinner 토큰화:** `common.css`, `member/my-memo.css`, `member/mypage.css`, `search.css`, `community/community.css` 5곳에 skeleton/spinner 패턴 사용 중. 1차 범위 외이지만 다크 확장 시 색상 토큰화 필요 (현재 라이트 회색 그라데이션 → 다크에서 하얗게 빛나 보일 위험)
- **이미지/SVG 자산 다크 변형:** 학습 페이지 등 이미지 비중이 큰 화면 확장 시 다크 변형 자산 또는 `filter` 적용 검토
- **`theme-color` 메타:** 모바일 브라우저 상단 chrome 색상 일치를 위해 `<meta name="theme-color">` 양 모드 분기 (`media="(prefers-color-scheme: ...)"`)
- **OG 이미지 분기:** 공유 시 다크 사용자에게 다크 OG 표시 (SEO 영향 검토 필요)
- **인쇄 스타일:** `@media print { :root { ...라이트 토큰... } }` 강제 적용
- **Analytics:** 토글 변경 이벤트를 `site_visit_event` (또는 신규 이벤트) 로 추적해 모드 선호도 측정

## 11. 관련 파일

### 신규
| 파일 | 역할 |
|---|---|
| `static/css/theme.css` | 라이트/다크 토큰 정의 |
| `static/js/theme-toggle.js` | 토글 처리, localStorage 동기화, matchMedia 리스너 |

### 수정
| 파일 | 변경 |
|---|---|
| `templates/fragments/head.html` | FOUC 방지 인라인 스크립트, `theme.css` `<link>` 추가 |
| `templates/fragments/header.html` | `topNavAccountMenu` 안 3개 토글 항목 추가 |
| `templates/index.html` | 캐시 버스팅 갱신 |
| `static/js/storage-util.js` | `STORAGE_KEYS.THEME_PREFERENCE` 추가, `ThemeStore` 헬퍼 |
| `static/css/common.css` | 토큰 치환 |
| `static/css/top-nav.css` | 토큰 치환 |
| `static/css/footer.css` | 토큰 치환 |
| `static/css/section-nav.css` | 토큰 치환 |
| `static/css/home.css` | 토큰 치환 |
| `static/css/hero.css` | 토큰 치환 |
| `static/css/card.css` | 토큰 치환 |
| `static/css/popular-search.css` | 토큰 치환 |

> 모든 수정 CSS 는 `?v=` 캐시 버스팅 갱신 필수. `head.html` 의 자동 로드 4종도 마찬가지.

## 12. 검증 체크리스트

- [ ] 새로고침 시 FOUC 없음 (라이트 → 다크 깜빡임 0)
- [ ] localStorage 차단 환경에서도 시스템 모드로 안전 폴백
- [ ] 시스템 따름 모드에서 OS 다크 토글 시 자동 반영
- [ ] 명시적 선택 후 OS 변경에 영향받지 않음
- [ ] 토글 메뉴 키보드 접근 (`Tab`/`Enter`/`Space`/`↑↓`)
- [ ] 스크린리더로 현재 선택 인지 가능 (`aria-checked`)
- [ ] WCAG AA 대비 모든 텍스트/UI 통과
- [ ] `prefers-reduced-motion` 환경에서 트랜지션 비활성
- [ ] `prefers-color-scheme` 미지원 브라우저에서 라이트 폴백
- [ ] Bootstrap utility 클래스(`text-muted`, `bg-light` 등) 다크에서 자연스러움 (`data-bs-theme` 동작 확인)
- [ ] Bootstrap 핵심 변수(`--bs-body-bg`, `--bs-body-color`, `--bs-border-color` 등) 프로젝트 토큰과 일관
- [ ] `useCommonCss=false` 페이지(admin, login 등) 에서도 토글 동작
- [ ] `section-nav.css` 의 기존 `:root` 변수가 시맨틱 토큰을 참조하도록 갱신됨
- [ ] 다크 모드에서 인쇄 시 라이트 강제 (`:root, html[data-theme="dark"]` 양쪽 override)
- [ ] 1차 범위 외 페이지 라이트 안전 유지
- [ ] 로그인/로그아웃 흐름에서 테마 유지
- [ ] 명시 선택 후 OS 변경 시 `change` 이벤트 무시 동작 검증
