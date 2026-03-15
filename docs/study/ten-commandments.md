# 십계명 (Ten Commandments) — 기획 및 설계 문서

> 작성일: 2026-03-15
> 담당 도메인: study
> 상태: 설계 완료 (구현 전)

---

## 1. 개요 (Overview)

### 1-1. 페이지 목적

출애굽기 20:1-17 / 신명기 5:6-21에 기록된 십계명을 카드 형식으로 나열하고, 각 계명의 원문·의미 해설·관련 구절을 제공하는 학습 페이지이다.
기존 '주기도문', '사도신경' 페이지와 동일한 UX 패턴(전문 표시 → 배경 설명 → 카드별 상세 해설)을 따른다.

### 1-2. URL 및 라우팅

| 항목 | 값 |
|---|---|
| URL | `/web/study/ten-commandments` |
| Thymeleaf 템플릿 | `templates/study/ten-commandments.html` |
| 컨트롤러 | `StudyWebController.kt` 내 `@GetMapping("/ten-commandments")` |
| 인증 | 불필요 (공개 페이지) |

### 1-3. 대상 사용자

- 십계명 전문과 의미를 학습하려는 새신자
- 교리 교육 자료로 활용하려는 교사·소그룹 리더
- 십계명의 신학적 맥락을 학습하려는 일반 성도

---

## 2. study.html 메뉴 추가

### 2-1. 카드 위치

기존 학습 허브(`study.html`) 카드 그리드에서 **'사도신경' 카드 다음**(9번째 → 10번째 위치)에 추가한다.
'준비중' 카드들 앞에 배치하여 활성 메뉴 영역에 포함시킨다.

### 2-2. 카드 HTML

```html
<div class="col-12 col-md-6 col-lg-4">
    <a class="text-decoration-none study-card-link card-lift" href="/web/study/ten-commandments">
        <div class="card card-panel card-soft">
            <div class="card-body">
                <h3 class="h6 fw-semibold mb-1"><span class="me-2" aria-hidden="true">⛰️</span>십계명</h3>
                <p class="text-muted small mb-0">출애굽기 20장의 십계명을 설명합니다.</p>
            </div>
        </div>
    </a>
</div>
```

### 2-3. 아이콘 선택 근거

`⛰️` (산) — 십계명이 시내산에서 주어진 것을 상징. 유니코드 이모지 사용으로 별도 SVG 불필요.

---

## 3. 파일 구조 (File Structure)

```
src/main/kotlin/com/elseeker/study/adapter/input/web/client/
└── StudyWebController.kt              ← @GetMapping("/ten-commandments") 추가

src/main/resources/
├── templates/study/
│   ├── study.html                     ← 십계명 카드 추가
│   └── ten-commandments.html          ← 신규 Thymeleaf 템플릿
├── static/css/study/
│   └── ten-commandments.css           ← 전용 CSS
└── static/js/study/
    └── ten-commandments.js            ← 데이터 + 렌더링 로직
```

### 버전 관리

| 파일 | 초기 버전 |
|---|---|
| `ten-commandments.css` | `?v=1.0` |
| `ten-commandments.js` | `?v=1.0` |
| `study.css` | 기존 버전 +0.1 |

---

## 4. 페이지 구조 (Page Structure)

주기도문·사도신경 페이지와 동일한 3단 구조를 따른다.

### 4-1. 전체 레이아웃

```
[Header — 뒤로가기 + "십계명" 타이틀]
[Section 1 — 십계명 전문 (Full Text)]
[Section 2 — 배경 설명 (History/Context)]
[Section 3 — 카드 그리드 (10개 계명 상세)]
[Scroll-to-top 버튼]
[Footer — 섹션 네비게이션]
```

### 4-2. Section 1 — 십계명 전문

전문을 한눈에 볼 수 있는 요약 패널. 보라~남색 계열 그라디언트 배경 (주기도문 패턴 재사용).

```html
<div id="tenCommandmentsFullText" class="ten-commandments-full-text">
    <div class="ten-commandments-full-text-inner">
        <h2 class="ten-commandments-full-text-title">십계명</h2>
        <p class="ten-commandments-full-text-ref">출애굽기 20:1-17</p>
        <div class="ten-commandments-full-text-body">
            <!-- 10개 계명 한 줄 요약 리스트 -->
        </div>
    </div>
</div>
```

**전문 요약 텍스트:**

```
제1계명  나 외에는 다른 신들을 네게 두지 말라
제2계명  너를 위하여 새긴 우상을 만들지 말라
제3계명  너의 하나님 여호와의 이름을 망령되게 부르지 말라
제4계명  안식일을 기억하여 거룩하게 지키라
제5계명  네 부모를 공경하라
제6계명  살인하지 말라
제7계명  간음하지 말라
제8계명  도둑질하지 말라
제9계명  네 이웃에 대하여 거짓 증거하지 말라
제10계명 네 이웃의 소유를 탐내지 말라
```

### 4-3. Section 2 — 배경 설명

십계명의 역사적·신학적 맥락을 설명하는 패널. 황색 계열 그라디언트 배경 (주기도문 history 패턴 재사용).

**배경 설명 항목:**

| # | 제목 | 내용 요약 |
|---|---|---|
| 1 | 성경적 출처 | 출애굽기 20:1-17(1차), 신명기 5:6-21(2차 선포). 두 본문의 미세한 차이 언급 |
| 2 | 수여 배경 | 이스라엘 백성이 이집트를 나온 후 시내산에서 하나님과 언약을 맺는 장면 |
| 3 | 두 돌판의 구분 | 전통적으로 1~4계명은 하나님과의 관계(1판), 5~10계명은 사람과의 관계(2판)로 구분 |
| 4 | 예수님의 요약 | 마태복음 22:37-40에서 예수님이 십계명을 사랑의 이중 계명으로 요약 |
| 5 | 신약에서의 의미 | 율법의 완성자로서의 그리스도 (마태복음 5:17) — 십계명은 폐지가 아닌 완성 |

### 4-4. Section 3 — 카드 그리드 (10장)

각 계명을 개별 카드로 표시. 카드 구조는 주기도문 카드와 동일한 패턴을 따른다.

**카드 내부 구조:**

```html
<div class="ten-commandments-card">
    <div class="ten-commandments-card-header">
        <span class="ten-commandments-card-order">{N}</span>
        <span class="ten-commandments-card-category">{대분류}</span>
    </div>
    <div class="ten-commandments-card-body">
        <blockquote class="ten-commandments-card-text">{계명 원문}</blockquote>
        <p class="ten-commandments-card-meaning">{해설}</p>
        <div class="ten-commandments-card-ref">
            <span class="ten-commandments-card-ref-label">{출처 구절}</span>
        </div>
        <div class="ten-commandments-card-related">
            <span class="ten-commandments-card-related-ref">{관련 신약 구절}</span>
            <p class="ten-commandments-card-related-text">{관련 구절 본문}</p>
        </div>
    </div>
</div>
```

---

## 5. 데이터 구조 (Data Structure)

### 5-1. JavaScript 데이터 배열

`ten-commandments.js`에 정적 배열로 정의한다 (DB 미사용, 클라이언트 전용 페이지).

```javascript
const TEN_COMMANDMENTS = [
    {
        order: 1,
        category: "하나님과의 관계",
        text: "나는 너를 애굽 땅, 종 되었던 집에서 인도하여 낸 네 하나님 여호와니라 너는 나 외에는 다른 신들을 네게 두지 말라",
        summary: "나 외에는 다른 신들을 네게 두지 말라",
        meaning: "...(해설)...",
        verse: "출애굽기 20:2-3",
        relatedVerse: "마태복음 4:10",
        relatedVerseText: "..."
    },
    // ... 2~10계명
];
```

### 5-2. 십계명 전체 데이터

| 계명 | 요약 | 출처 | 대분류 | 관련 신약 구절 |
|---|---|---|---|---|
| 1 | 나 외에는 다른 신들을 네게 두지 말라 | 출 20:2-3 | 하나님과의 관계 | 마태복음 4:10 |
| 2 | 너를 위하여 새긴 우상을 만들지 말라 | 출 20:4-6 | 하나님과의 관계 | 요한일서 5:21 |
| 3 | 여호와의 이름을 망령되게 부르지 말라 | 출 20:7 | 하나님과의 관계 | 마태복음 5:33-37 |
| 4 | 안식일을 기억하여 거룩하게 지키라 | 출 20:8-11 | 하나님과의 관계 | 마가복음 2:27-28 |
| 5 | 네 부모를 공경하라 | 출 20:12 | 사람과의 관계 | 에베소서 6:1-3 |
| 6 | 살인하지 말라 | 출 20:13 | 사람과의 관계 | 마태복음 5:21-22 |
| 7 | 간음하지 말라 | 출 20:14 | 사람과의 관계 | 마태복음 5:27-28 |
| 8 | 도둑질하지 말라 | 출 20:15 | 사람과의 관계 | 에베소서 4:28 |
| 9 | 거짓 증거하지 말라 | 출 20:16 | 사람과의 관계 | 에베소서 4:25 |
| 10 | 네 이웃의 소유를 탐내지 말라 | 출 20:17 | 사람과의 관계 | 히브리서 13:5 |

### 5-3. 배경 설명 데이터

```javascript
const TEN_COMMANDMENTS_HISTORY = [
    {
        title: "1. 성경적 출처",
        body: "십계명은 출애굽기 20:1-17과 신명기 5:6-21에 두 번 기록되어 있다. 출애굽기는 시내산 언약 체결 시점의 원본이며, 신명기는 모세가 40년 광야 생활 이후 새 세대에게 다시 선포한 것이다. 두 본문은 대부분 동일하나, 안식일 계명(제4계명)의 근거가 다르다: 출애굽기는 창조(하나님의 안식)를, 신명기는 구원(이집트에서의 해방)을 근거로 제시한다."
    },
    {
        title: "2. 시내산 언약",
        body: "이스라엘 백성이 이집트를 탈출한 지 약 3개월 후, 시내산(호렙산)에서 하나님이 모세를 통해 십계명을 주셨다(출애굽기 19-20장). 이는 하나님이 이스라엘과 맺은 언약의 핵심 조항으로, 하나님의 구원 행위('내가 너를 이집트에서 인도하여 낸')가 계명에 앞서 선언된다. 즉, 십계명은 구원받기 위한 조건이 아니라 구원받은 백성의 응답이다."
    },
    {
        title: "3. 두 돌판의 구분",
        body: "십계명은 두 돌판에 새겨졌다(출애굽기 31:18). 전통적으로 제1~4계명은 하나님과 사람의 관계(수직적 관계)를, 제5~10계명은 사람과 사람의 관계(수평적 관계)를 다루는 것으로 구분한다. 이 구분은 예수님의 이중 사랑 계명(마태복음 22:37-40)과 정확히 대응한다."
    },
    {
        title: "4. 예수님의 요약",
        body: "예수님은 마태복음 22:37-40에서 '네 마음을 다하고 목숨을 다하고 뜻을 다하여 주 너의 하나님을 사랑하라'(제1~4계명 요약)와 '네 이웃을 네 자신 같이 사랑하라'(제5~10계명 요약)로 온 율법과 선지자의 강령을 두 마디로 요약하셨다."
    },
    {
        title: "5. 신약에서의 의미",
        body: "예수님은 '내가 율법이나 선지자를 폐하러 온 줄로 생각하지 말라 폐하러 온 것이 아니요 완전하게 하려 함이라'(마태복음 5:17)고 말씀하셨다. 바울은 '사랑은 율법의 완성'(로마서 13:10)이라고 선언한다. 그리스도인에게 십계명은 정죄의 도구가 아니라, 하나님의 성품을 반영하는 사랑의 원리이며 성화의 지침이다."
    }
];
```

---

## 6. 시각 디자인 (Visual Design)

### 6-1. 디자인 방향

주기도문·사도신경 페이지와 일관된 디자인 시스템을 유지하되, 십계명 고유의 색상 테마를 적용한다.

### 6-2. 색상 팔레트

| 영역 | 색상 | 용도 |
|---|---|---|
| 전문 패널 배경 | `linear-gradient(145deg, #ede9fe, #e0e7ff)` | 주기도문과 동일 (보라~남색 계열) |
| 배경 설명 패널 | `linear-gradient(145deg, #fef3c7, #fde68a)` | 주기도문과 동일 (황색 계열) |
| 카드 헤더 (1판: 1~4계명) | `linear-gradient(145deg, #dbeafe, #bfdbfe)` | 하늘색 계열 — 하나님과의 관계 |
| 카드 헤더 (2판: 5~10계명) | `linear-gradient(145deg, #d1fae5, #a7f3d0)` | 초록색 계열 — 사람과의 관계 |
| 카드 순서 번호 (1판) | `color: #1e40af` / `bg: #fff` | 파란색 |
| 카드 순서 번호 (2판) | `color: #065f46` / `bg: #fff` | 초록색 |
| 카드 텍스트 인용 좌측 선 (1판) | `#3b82f6` | 파란색 |
| 카드 텍스트 인용 좌측 선 (2판) | `#10b981` | 초록색 |

> **설계 근거:** 두 돌판(하나님 관계 / 사람 관계)의 구분을 색상으로 시각적으로 표현하여, 십계명의 이중 구조를 직관적으로 전달한다.

### 6-3. 타이포그래피

주기도문 페이지와 동일한 타이포그래피 규격을 따른다.

| 요소 | 크기 | 굵기 |
|---|---|---|
| 전문 제목 | 1.25rem | 700 |
| 전문 출처 | 0.8rem | 400 |
| 전문 본문 | 0.95rem, line-height 2 | 400 |
| 카드 계명 텍스트 | 1.05rem | 600 |
| 카드 해설 | 0.85rem, line-height 1.7 | 400 |

---

## 7. 반응형 디자인 (Responsive Design)

주기도문·사도신경 페이지와 동일한 반응형 전략을 따른다.

### 모바일 (<=576px)

- 전문 패널 패딩: `1.25rem 1rem`
- 전문 제목: `1.1rem`
- 카드 그리드 gap: `1rem`
- 카드 헤더 패딩: `0.75rem 0.85rem`
- 카드 본문 패딩: `0.85rem`
- 계명 텍스트: `0.95rem`

### 데스크탑

- 콘텐츠 최대 너비: `720px`, 중앙 정렬
- 카드 hover 효과: `box-shadow + translateY(-2px)` (`@media (hover: hover) and (pointer: fine)`)

---

## 8. JavaScript 클래스 구조

주기도문(`LordsPrayer`)과 동일한 클래스 구조를 따른다.

```javascript
class TenCommandments {
    constructor() {
        this.initElements();
        this.init();
    }

    initElements() {
        this.loadingEl = document.getElementById("tenCommandmentsLoading");
        this.contentEl = document.getElementById("tenCommandmentsContent");
        this.fullTextEl = document.getElementById("tenCommandmentsFullText");
        this.historyEl = document.getElementById("tenCommandmentsHistory");
        this.gridEl = document.getElementById("tenCommandmentsGrid");
        this.backButton = document.getElementById("topNavBackButton");
        this.scrollToTopBtn = document.getElementById("scrollToTopBtn");
    }

    init() {
        this.initNav();          // "십계명" 타이틀 + 뒤로가기 버튼
        this.initScrollToTop();  // 스크롤 투 탑 버튼
        this.render();
    }

    initNav() { /* pageTitleLabel = "십계명", backButton → /web/study */ }
    initScrollToTop() { /* window.scrollY >= 300 → is-visible 토글 */ }

    render() {
        this.renderFullText();    // 십계명 전문
        this.renderHistory();     // 배경 설명
        this.renderCards();       // 10개 카드
        this.loadingEl.classList.add("d-none");
        this.contentEl.classList.remove("d-none");
    }

    renderFullText() { /* 10계명 한 줄 요약 리스트 */ }
    renderHistory() { /* TEN_COMMANDMENTS_HISTORY 렌더링 */ }
    renderCards() { /* TEN_COMMANDMENTS 배열 순회, createCard() 호출 */ }

    createCard(item, order) {
        // 1~4: 하나님 관계 → 파란색 헤더
        // 5~10: 사람 관계 → 초록색 헤더
        const isFirstTablet = order <= 4;
        // ... 카드 DOM 생성
    }
}

document.addEventListener("DOMContentLoaded", () => {
    new TenCommandments();
});
```

---

## 9. Thymeleaf 템플릿

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="ko">
<head th:replace="~{fragments/head :: head('십계명 전문과 의미 해설 | ElSeeker', true,
        '/css/search.css?v=2.2,/css/card.css?v=2.3,/css/study/ten-commandments.css?v=1.0')}"
      th:with="pageDescription='출애굽기 20장 십계명 전문과 각 계명의 의미를 해설하는 학습 페이지입니다.',
               pageKeywords='십계명,십계명 전문,십계명 뜻,십계명 해설,출애굽기 20장,모세 십계명'"></head>
<body class="has-fixed-nav has-dual-bottom-nav">

<header th:replace="~{fragments/header :: header}"></header>

<main class="container content-wrapper">
    <!-- 로딩 상태 -->
    <div id="tenCommandmentsLoading" class="ten-commandments-loading text-center py-5">
        <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-3 text-muted">십계명을 불러오는 중입니다...</p>
    </div>

    <!-- 콘텐츠 -->
    <div id="tenCommandmentsContent" class="d-none">
        <div id="tenCommandmentsFullText" class="ten-commandments-full-text"></div>
        <div id="tenCommandmentsHistory" class="ten-commandments-history"></div>
        <div id="tenCommandmentsGrid" class="ten-commandments-grid"></div>
    </div>
</main>

<button id="scrollToTopBtn" class="scroll-to-top-btn" type="button"
        aria-label="맨 위로 이동">↑</button>

<script type="module" src="/js/study/ten-commandments.js?v=1.0"></script>
<div th:replace="~{fragments/section-nav :: section-nav}"></div>
</body>
</html>
```

---

## 10. 컨트롤러 변경

`StudyWebController.kt`에 라우트 1개 추가:

```kotlin
@GetMapping("/ten-commandments")
fun showTenCommandments(): String {
    return "study/ten-commandments"
}
```

---

## 11. 접근성 (Accessibility)

- `prefers-reduced-motion: reduce` 지원: hover 트랜지션 비활성화
- 카드의 `<blockquote>`에 계명 원문 텍스트 — 스크린 리더 접근 가능
- 스크롤 투 탑 버튼: `aria-label="맨 위로 이동"`
- 이모지 아이콘: `aria-hidden="true"` 적용
- 색상 대비: WCAG AA 기준 충족 (해설 텍스트 `#334155` on `#ffffff` = 대비비 7.3:1)

---

## 12. SEO

### 12-1. 메타 태그

```
title:       "십계명 전문과 의미 해설 | ElSeeker"
description: "출애굽기 20장 십계명 전문과 각 계명의 의미를 해설하는 학습 페이지입니다."
keywords:    "십계명,십계명 전문,십계명 뜻,십계명 해설,출애굽기 20장,모세 십계명"
```

### 12-2. sitemap.xml 추가

```xml
<url>
    <loc>https://elseeker.com/web/study/ten-commandments</loc>
    <changefreq>monthly</changefreq>
    <priority>0.7</priority>
</url>
```

### 12-3. study.html 메타 태그 업데이트

페이지 제목과 키워드에 '십계명' 추가:

```
title:    "성경 학습 - 사전, 족보, 역사, 개요 영상, 십계명 | ElSeeker"
keywords: "성경 학습,...,십계명"
```

---

## 13. 구현 체크리스트

- [ ] `StudyWebController.kt` — `@GetMapping("/ten-commandments")` 추가
- [ ] `study.html` — 십계명 카드 추가 (사도신경 카드 다음)
- [ ] `study.html` — 메타 태그에 '십계명' 키워드 추가
- [ ] `ten-commandments.html` — Thymeleaf 템플릿 생성
- [ ] `ten-commandments.css` — 전용 CSS 생성 (주기도문 CSS 기반 + 두 돌판 색상)
- [ ] `ten-commandments.js` — 데이터 + 렌더링 클래스 생성
- [ ] `sitemap.xml` — 십계명 URL 추가
- [ ] `study.css` 수정 시 `?v=` 버전 올림

---

## 14. 설계 결정 기록 (Design Decision Log)

| 결정 | 채택 | 기각 | 이유 |
|---|---|---|---|
| 데이터 저장 방식 | JS 정적 배열 (클라이언트 전용) | DB + API | 주기도문·사도신경과 동일 패턴. 변경 빈도 거의 없는 고정 콘텐츠 |
| 카드 색상 구분 | 1~4계명 파랑, 5~10계명 초록 | 전체 동일 색상 | 두 돌판(하나님/사람 관계) 구조를 시각적으로 전달 |
| 아이콘 | ⛰️ 유니코드 이모지 | SVG 커스텀 아이콘 | 시내산 상징. 다른 학습 메뉴(주기도문 🙏, 사도신경 ✝️)와 일관된 이모지 사용 |
| 페이지 구조 | 전문 → 배경 → 카드 (3단 구조) | 스크롤 스토리텔링 | 10개 항목의 상세 해설이 목적이므로, 카드 나열이 탐색에 유리 |
| 카드 위치 (study.html) | 사도신경 다음 | 최하단 | 교리 학습 콘텐츠(주기도문→사도신경→십계명)가 연속 배치되는 것이 논리적 |

---

## 15. 참고 자료

- 주기도문 구현 패턴: `templates/study/lords-prayer.html`, `js/study/lords-prayer.js`, `css/study/lords-prayer.css`
- 사도신경 구현 패턴: `templates/study/apostles-creed.html`, `js/study/apostles-creed.js`, `css/study/apostles-creed.css`
- 컨트롤러 패턴: `StudyWebController.kt`
- 학습 허브 카드 패턴: `templates/study/study.html`
- 7일 창조 설계 문서: `docs/common/creation-scroll.md`
