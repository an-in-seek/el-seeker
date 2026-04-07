# 공동체성경읽기 (Public Reading of Scripture) — 기획 및 설계 문서

> 작성일: 2026-04-07
> 담당 도메인: study
> 상태: 초안

---

## 1. 개요 (Overview)

### 1-1. 공동체성경읽기(PRS)란?

공동체성경읽기(Public Reading of Scripture, PRS)는 성경을 **'공동체가 함께 듣고 입체적으로 경험'**하도록 설계된 성경 통독 운동이자 플랫폼이다.

**드라마바이블의 압도적 퀄리티**
- 전문 연기자(차인표, 신애라 등)와 성우가 참여하여 각 인물의 감정을 살려 낭독
- 영화처럼 배경음악(OST)과 효과음이 깔려 있어 성경 속 현장에 있는 듯한 몰입감 제공
- 텍스트와 함께 배경 이미지가 제공되어 눈과 귀가 동시에 성경에 집중

**'Just Show Up(그냥 오기만 하세요)' 철학**
- 성경이 원래 '읽기'보다 '듣기'를 위해 기록되었다는 점에 착안
- 혼자 성경을 읽기 힘들어하는 사람들을 위해, 정해진 분량의 영상을 틀어놓고 '함께 듣기만 해도 성경 통독이 되도록' 가이드라인 제공
- 1년 1독, 200일 통독 등 다양한 스케줄에 맞춘 재생목록 구성

**콘텐츠 구성**
- 매일 성경 읽기: 그날 분량의 영상이 재생목록으로 정리되어 있음
- 묵상과 기도: 성경 읽기 전후 시편 찬양이나 짧은 기도가 포함된 영상 제공
- 다양한 언어 지원: 글로벌 PRS 운동의 일환으로 한국어뿐 아니라 영어, 중국어 등 다양한 언어 버전 연결

### 1-2. 페이지 목적

PRS 드라마바이블 영상을 성경 책별로 모아 제공하는 **클라이언트 전용** 학습 페이지이다.
`bible-overview-video` 페이지와 동일한 패턴으로, 서버 API 없이 정적 JS 배열에 유튜브 링크를 관리하고 썸네일 카드 그리드로 렌더링한다.

### 1-3. URL 및 라우팅

| 항목 | 값 |
|---|---|
| URL | `/web/study/public-reading-of-scripture` |
| Thymeleaf 템플릿 | `templates/study/public-reading-of-scripture.html` |
| 컨트롤러 | `StudyWebController.kt` 내 `@GetMapping("/public-reading-of-scripture")` |
| 인증 | 불필요 (공개 페이지) |

### 1-4. 대상 사용자

- 공동체성경읽기(PRS) 프로그램에 참여 중이거나 관심 있는 성도
- 혼자 성경 읽기가 어려워 드라마바이블로 '듣는 통독'을 시작하려는 새신자
- 소그룹/가정 예배에서 드라마바이블 영상을 활용하려는 리더
- 성경 각 책의 드라마 낭독을 몰입감 있게 시청하려는 학습자

---

## 2. study.html 메뉴 추가

### 2-1. 카드 위치

기존 학습 허브(`study.html`) 카드 그리드에서 **'성주간 타임라인' 카드 다음**(11번째 → 12번째 위치)에 추가한다.
'준비중' 카드들 앞에 배치하여 활성 메뉴 영역에 포함시킨다.

### 2-2. 카드 HTML

```html
<div class="col-12 col-md-6 col-lg-4">
    <a class="text-decoration-none study-card-link card-lift" href="/web/study/public-reading-of-scripture">
        <div class="card card-panel card-soft">
            <div class="card-body">
                <h3 class="h6 fw-semibold mb-1">
                    <img src="/images/icon/youtube.svg" class="me-2" width="24" height="24" alt="YouTube Icon" loading="lazy" decoding="async">
                    공동체성경읽기</h3>
                <p class="text-muted small mb-0">드라마바이블로 성경을 함께 듣습니다.</p>
            </div>
        </div>
    </a>
</div>
```

---

## 3. 파일 구조

`bible-overview-video`와 동일한 구성을 따른다.

```
src/main/resources/
├── templates/study/public-reading-of-scripture.html   — Thymeleaf 템플릿
├── static/css/study/public-reading-of-scripture.css   — 전용 CSS
└── static/js/study/public-reading-of-scripture.js     — 정적 데이터 + 렌더링 로직
```

서버 코드 변경은 `StudyWebController.kt`에 `@GetMapping` 1건 추가만 필요하다.

---

## 4. 화면 구성

### 4-1. 전체 레이아웃

`bible-overview-video.html`과 동일한 구조:

```
[header]
[로딩 스피너]
[검색 바]
[구약 섹션 - 카드 그리드]
[신약 섹션 - 카드 그리드]
[section-nav]
```

### 4-2. Thymeleaf 템플릿 (`public-reading-of-scripture.html`)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="ko">
<head th:replace="~{fragments/head :: head('공동체성경읽기 - 책별 해설 영상 | ElSeeker', true,
      '/css/hero.css?v=2.2,/css/card.css?v=2.3,/css/book-search.css?v=1.0,/css/study/public-reading-of-scripture.css?v=1.0')}"
      th:with="pageDescription='공동체성경읽기(PRS) 드라마바이블 영상을 성경 책별로 시청할 수 있는 학습 페이지입니다. 전문 배우와 성우의 낭독, OST, 효과음으로 몰입감 있는 성경 통독을 경험하세요.',
               pageKeywords='공동체성경읽기,PRS,드라마바이블,성경 낭독,성경 듣기,성경 통독,성경 영상'">
</head>
<body class="has-fixed-nav has-dual-bottom-nav">

<header th:replace="~{fragments/header :: header}"></header>

<main class="container content-wrapper">
    <!-- 로딩 상태 -->
    <div id="videoLoading" class="prs-loading text-center py-5">
        <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-3 text-muted">영상 목록을 불러오는 중입니다...</p>
    </div>

    <!-- 영상 목록 -->
    <div id="videoContent" class="d-none">
        <!-- 책 검색 -->
        <div class="book-search-wrapper">
            <div class="book-search-input-group">
                <svg class="book-search-icon" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                    <path fill-rule="evenodd" d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z" clip-rule="evenodd"/>
                </svg>
                <input type="search" id="bookSearchInput" class="book-search-input"
                       placeholder="책 이름 검색 (예: 창세기, 마태)" autocomplete="off" inputmode="search">
                <button type="button" id="bookSearchClear" class="book-search-clear d-none" aria-label="검색어 지우기">
                    <svg viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                        <path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z"/>
                    </svg>
                </button>
            </div>
            <div id="bookSearchEmpty" class="book-search-empty d-none">검색 결과가 없습니다.</div>
        </div>

        <section class="prs-section" id="oldTestamentSection">
            <h2 class="prs-section-title">구약</h2>
            <div class="prs-grid" id="oldTestamentGrid"></div>
        </section>

        <section class="prs-section" id="newTestamentSection">
            <h2 class="prs-section-title">신약</h2>
            <div class="prs-grid" id="newTestamentGrid"></div>
        </section>
    </div>
</main>

<script type="module" src="/js/study/public-reading-of-scripture.js?v=1.0"></script>
<div th:replace="~{fragments/section-nav :: section-nav}"></div>
</body>
</html>
```

---

## 5. JavaScript 데이터 및 로직 (`public-reading-of-scripture.js`)

### 5-1. 정적 데이터 배열

`bible-overview-video.js`의 `BIBLE_VIDEOS`와 동일한 구조. 영상이 없는 책은 `youtubeUrl: ""`으로 비워 "준비중" 표시한다.

```javascript
const PRS_VIDEOS = [
    // ── 구약 (39권) ──
    {bookOrder: 1,  bookName: "창세기",       youtubeUrl: "https://youtu.be/NbGHNcPhlUY?si=h9WLvhkuNUyrOYXz"},
    {bookOrder: 2,  bookName: "출애굽기",     youtubeUrl: "https://youtu.be/pPqhb_cVZT8?si=6OP8XpUnPxaPxguN"},
    {bookOrder: 3,  bookName: "레위기",       youtubeUrl: "https://youtu.be/w5_gU3NnsVw?si=hKV8aXPVdB4dixWI"},
    {bookOrder: 4,  bookName: "민수기",       youtubeUrl: "https://youtu.be/nyw5Qki5SIw?si=TbN0jHOJ7FveGDr7"},
    {bookOrder: 5,  bookName: "신명기",       youtubeUrl: "https://youtu.be/hRRWuQHVTe0?si=d8tnP2IsufUPllFf"},
    {bookOrder: 6,  bookName: "여호수아",     youtubeUrl: ""},
    {bookOrder: 7,  bookName: "사사기",       youtubeUrl: ""},
    {bookOrder: 8,  bookName: "룻기",         youtubeUrl: ""},
    {bookOrder: 9,  bookName: "사무엘상",     youtubeUrl: ""},
    {bookOrder: 10, bookName: "사무엘하",     youtubeUrl: ""},
    {bookOrder: 11, bookName: "열왕기상",     youtubeUrl: ""},
    {bookOrder: 12, bookName: "열왕기하",     youtubeUrl: ""},
    {bookOrder: 13, bookName: "역대상",       youtubeUrl: ""},
    {bookOrder: 14, bookName: "역대하",       youtubeUrl: ""},
    {bookOrder: 15, bookName: "에스라",       youtubeUrl: ""},
    {bookOrder: 16, bookName: "느헤미야",     youtubeUrl: ""},
    {bookOrder: 17, bookName: "에스더",       youtubeUrl: ""},
    {bookOrder: 18, bookName: "욥기",         youtubeUrl: ""},
    {bookOrder: 19, bookName: "시편",         youtubeUrl: ""},
    {bookOrder: 20, bookName: "잠언",         youtubeUrl: ""},
    {bookOrder: 21, bookName: "전도서",       youtubeUrl: ""},
    {bookOrder: 22, bookName: "아가",         youtubeUrl: ""},
    {bookOrder: 23, bookName: "이사야",       youtubeUrl: ""},
    {bookOrder: 24, bookName: "예레미야",     youtubeUrl: ""},
    {bookOrder: 25, bookName: "예레미야애가", youtubeUrl: ""},
    {bookOrder: 26, bookName: "에스겔",       youtubeUrl: ""},
    {bookOrder: 27, bookName: "다니엘",       youtubeUrl: ""},
    {bookOrder: 28, bookName: "호세아",       youtubeUrl: ""},
    {bookOrder: 29, bookName: "요엘",         youtubeUrl: ""},
    {bookOrder: 30, bookName: "아모스",       youtubeUrl: ""},
    {bookOrder: 31, bookName: "오바댜",       youtubeUrl: ""},
    {bookOrder: 32, bookName: "요나",         youtubeUrl: ""},
    {bookOrder: 33, bookName: "미가",         youtubeUrl: ""},
    {bookOrder: 34, bookName: "나훔",         youtubeUrl: ""},
    {bookOrder: 35, bookName: "하박국",       youtubeUrl: ""},
    {bookOrder: 36, bookName: "스바냐",       youtubeUrl: ""},
    {bookOrder: 37, bookName: "학개",         youtubeUrl: ""},
    {bookOrder: 38, bookName: "스가랴",       youtubeUrl: ""},
    {bookOrder: 39, bookName: "말라기",       youtubeUrl: ""},
    // ── 신약 (27권) ──
    {bookOrder: 40, bookName: "마태복음",       youtubeUrl: ""},
    {bookOrder: 41, bookName: "마가복음",       youtubeUrl: ""},
    {bookOrder: 42, bookName: "누가복음",       youtubeUrl: ""},
    {bookOrder: 43, bookName: "요한복음",       youtubeUrl: ""},
    {bookOrder: 44, bookName: "사도행전",       youtubeUrl: ""},
    {bookOrder: 45, bookName: "로마서",         youtubeUrl: ""},
    {bookOrder: 46, bookName: "고린도전서",     youtubeUrl: ""},
    {bookOrder: 47, bookName: "고린도후서",     youtubeUrl: ""},
    {bookOrder: 48, bookName: "갈라디아서",     youtubeUrl: ""},
    {bookOrder: 49, bookName: "에베소서",       youtubeUrl: ""},
    {bookOrder: 50, bookName: "빌립보서",       youtubeUrl: ""},
    {bookOrder: 51, bookName: "골로새서",       youtubeUrl: ""},
    {bookOrder: 52, bookName: "데살로니가전서", youtubeUrl: ""},
    {bookOrder: 53, bookName: "데살로니가후서", youtubeUrl: ""},
    {bookOrder: 54, bookName: "디모데전서",     youtubeUrl: ""},
    {bookOrder: 55, bookName: "디모데후서",     youtubeUrl: ""},
    {bookOrder: 56, bookName: "디도서",         youtubeUrl: ""},
    {bookOrder: 57, bookName: "빌레몬서",       youtubeUrl: ""},
    {bookOrder: 58, bookName: "히브리서",       youtubeUrl: ""},
    {bookOrder: 59, bookName: "야고보서",       youtubeUrl: ""},
    {bookOrder: 60, bookName: "베드로전서",     youtubeUrl: ""},
    {bookOrder: 61, bookName: "베드로후서",     youtubeUrl: ""},
    {bookOrder: 62, bookName: "요한1서",        youtubeUrl: ""},
    {bookOrder: 63, bookName: "요한2서",        youtubeUrl: ""},
    {bookOrder: 64, bookName: "요한3서",        youtubeUrl: ""},
    {bookOrder: 65, bookName: "유다서",         youtubeUrl: ""},
    {bookOrder: 66, bookName: "요한계시록",     youtubeUrl: ""},
];
```

### 5-2. 클래스 구조

`BibleOverviewVideo` 클래스를 그대로 참고하여 `PublicReadingOfScripture` 클래스를 구현한다.

```javascript
class PublicReadingOfScripture {
    constructor() {
        this.initElements();
        this.init();
    }

    initElements()       { /* DOM 요소 캐싱 */ }
    init()               { /* initNav → render → initBookSearch → scrollToTargetBook */ }
    initNav()            { /* 뒤로가기 버튼, 페이지 타이틀 "공동체성경읽기" 설정 */ }
    render()             { /* 구약(bookOrder <= 39)/신약(bookOrder >= 40) 분리 → createCard → 그리드 삽입 */ }
    extractVideoId(url)  { /* youtu.be URL에서 video ID 추출 */ }
    createCard(book)     { /* 썸네일 카드 or 준비중 카드 생성 */ }
    initBookSearch()     { /* 검색 입력 이벤트 바인딩 */ }
    filterBooks(keyword) { /* 키워드 필터링 → 그리드 재렌더링 */ }
    scrollToTargetBook() { /* ?bookOrder= 파라미터로 스크롤 + 스포트라이트 */ }
}

document.addEventListener("DOMContentLoaded", () => {
    new PublicReadingOfScripture();
});
```

---

## 6. CSS (`public-reading-of-scripture.css`)

`bible-overview-video.css`와 동일한 스타일을 적용한다. CSS 클래스 접두사를 `prs-`로 변경한다.

| bible-overview-video | public-reading-of-scripture |
|---|---|
| `.bible-overview-video-loading` | `.prs-loading` |
| `.bible-overview-video-section` | `.prs-section` |
| `.bible-overview-video-section-title` | `.prs-section-title` |
| `.bible-overview-video-grid` | `.prs-grid` |
| `.bible-overview-video-card` | `.prs-card` |
| `.bible-overview-video-thumb` | `.prs-thumb` |
| `.bible-overview-video-play` | `.prs-play` |
| `.bible-overview-video-book-name` | `.prs-book-name` |
| `.bible-overview-video-badge` | `.prs-badge` |
| `.video-spotlight-overlay` | `.prs-spotlight-overlay` |
| `.is-spotlight-target` | `.is-spotlight-target` (공통) |

---

## 7. 컨트롤러 변경

`StudyWebController.kt`에 1건 추가:

```kotlin
@GetMapping("/public-reading-of-scripture")
fun publicReadingOfScripture(): String {
    return "study/public-reading-of-scripture"
}
```

---

## 8. chapter-list.html 연동

`bible-overview-video`와 동일하게, 장 목록 페이지에서 해당 책의 공동체성경읽기 영상으로 이동하는 버튼을 추가할 수 있다.
(선택사항 — 1단계에서는 생략 가능)

```
/web/study/public-reading-of-scripture?bookOrder={bookOrder}&from=chapter-list
```

---

## 9. 유튜브 영상 링크 (확보 분)

현재 모세오경 5권의 영상이 확보되어 있으며, 나머지 책은 확보 시 JS 배열에 URL을 추가한다.

| bookOrder | 책 | YouTube URL |
|---|---|---|
| 1 | 창세기 | https://youtu.be/NbGHNcPhlUY?si=h9WLvhkuNUyrOYXz |
| 2 | 출애굽기 | https://youtu.be/pPqhb_cVZT8?si=6OP8XpUnPxaPxguN |
| 3 | 레위기 | https://youtu.be/w5_gU3NnsVw?si=hKV8aXPVdB4dixWI |
| 4 | 민수기 | https://youtu.be/nyw5Qki5SIw?si=TbN0jHOJ7FveGDr7 |
| 5 | 신명기 | https://youtu.be/hRRWuQHVTe0?si=d8tnP2IsufUPllFf |

---

## 10. 구현 체크리스트

- [ ] `StudyWebController.kt`에 `@GetMapping("/public-reading-of-scripture")` 추가
- [ ] `public-reading-of-scripture.html` 템플릿 생성
- [ ] `public-reading-of-scripture.css` 스타일 생성 (`prs-` 접두사)
- [ ] `public-reading-of-scripture.js` 데이터 배열 + 렌더링 클래스 생성
- [ ] `study.html`에 메뉴 카드 추가
- [ ] `study.html` CSS 캐시 버스팅 버전 업
- [ ] 검색 기능 동작 확인
- [ ] 모바일/데스크톱 반응형 확인
