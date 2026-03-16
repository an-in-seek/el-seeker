# ElSeeker

ElSeeker는 "하나님을 구하는 사람" 또는 "하나님을 찾는 사람"이라는 의미를 지닌 성경 플랫폼 서비스입니다. Kotlin과 Spring Boot 기반으로 개발되었으며 REST API와 Thymeleaf 기반 웹 UI를 통해 성경 번역본, 책, 장, 절 데이터를
조회하고 검색할 수 있습니다.
성경 퀴즈 스테이지와 스터디(사전/역사) 콘텐츠를 제공하고, 기본은 인메모리 H2 데이터베이스를 사용하며 애플리케이션 시작 시 SQL 시드 파일로 초기 데이터를 로딩합니다.

## 브랜드/철학

ElSeeker는 "하나님을 구하는 사람/하나님을 찾는 사람"이라는 의미를 바탕으로, 성경을 통해 하나님을 알아가는 여정을 돕는 플랫폼을 지향합니다.

## 주요 기능

### 성경 (Bible)

* 번역본/책/장/절 계층 탐색 웹 UI 및 REST API 제공 (KRV 66권 전체, NKRV 일부, KJV 목록)
* 성경 구절 키워드 검색 (번역본 단위, 페이지네이션 지원)
* 오늘의 말씀 — 매일 변경되는 랜덤 구절 조회 API
* 이전/다음 장 네비게이션 API
* 책 소개(Book Description) 페이지 — 각 성경 책의 요약 정보 제공
* 구절 메모 — 장별 메모 조회·등록·삭제 (로그인 필수)
* 구절 하이라이트 — 장별 하이라이트 조회·등록·삭제 (로그인 필수)
* 읽기 진행도 — 장 단위 읽기 기록 조회·등록 (로그인 필수)

### 게임 (Game)

* **성경 퀴즈** — 스테이지별 문항 풀기, 난이도 선택, 점수 기록, 진행도 초기화
* **성경 OX 퀴즈** — 참/거짓 문항 스테이지, 문항별 즉시 채점, 스테이지 완료 기록
* **성경 타자 연습** — 번역본/책/장 단위 타자 세션 생성·종료, 절별 진행도 저장, 정확도·속도 측정, 최근 진행 이어하기
* **성경 뽑기** — 랜덤 구절 추첨
* **성경 단어 퍼즐** — 난이도별 낱말 퍼즐 목록 조회, 플레이(자동 저장·이어하기), 힌트(글자 공개·단어 확인), 제출 채점 및 결과 학습

### 학습 (Study)

* **성경 사전** — 성경 용어 키워드 검색, 상세 조회 (REST API 및 웹 UI)
* **성경 역사** — 시대별 타임라인 탐색, 시대 상세, 개별 사건 상세 (정치/문화적 배경, 성경 참조)
* **성경 개요 영상** — 66권 유튜브 영상 목록 (클라이언트 전용, 정적 데이터)
* **성경 족보** — 마태복음/누가복음 족보 비교 (클라이언트 전용, 정적 데이터)
* **12사도** — 예수님의 12제자 학습 페이지
* **12지파** — 이스라엘 12지파 학습 페이지
* **사도신경** — 사도신경 내용 학습 페이지
* **주기도문** — 주기도문 내용 학습 페이지

### 커뮤니티 (Community)

* 게시글 작성·수정·삭제, 목록 조회(페이지네이션), 상세 조회
* 인기 게시글(Top Posts) 조회
* 댓글 작성·수정·삭제
* 리액션(좋아요 등) 등록·취소
* 게시글·댓글 신고

### 회원 (Member)

* OAuth2 소셜 로그인 (Google, Naver, Kakao)
* JWT 기반 무상태 인증 — Access/Refresh 토큰 HttpOnly 쿠키 발급, 자동 갱신
* 마이페이지 — 프로필(닉네임·프로필 이미지) 조회·수정
* OAuth 계정 관리 — 연동 계정 목록 조회, 추가 연동, 연동 해제 (최초 가입 계정 보호)
* OAuth 프로필 초기화 — 특정 연동 계정의 닉네임·이메일로 프로필 설정
* 회원 탈퇴 — 계정 및 연관 데이터 영구 삭제

### 관리자 (Admin)

* **성경 관리** — 번역본, 책, 책 소개, 장, 절 CRUD
* **사전 관리** — 성경 사전 항목 CRUD (키워드 검색)
* **단어 퍼즐 관리** — 퍼즐 CRUD, 퍼즐 항목(Entry) CRUD
* **퀴즈 관리** — 퀴즈 스테이지·문항 CRUD
* **OX 퀴즈 관리** — OX 스테이지·문항 CRUD
* **커뮤니티 관리** — 게시글 CRUD·상태 변경, 댓글 상태 변경·삭제·복원, 신고 목록 조회
* **회원 관리** — 회원 목록 조회(검색), 회원 정보 수정

### 기타

* 이용약관 및 개인정보처리방침 페이지
* SpringDoc 기반 OpenAPI 및 Swagger UI 제공

## 기술 스택

* Kotlin 1.9.25, Spring Boot 3.5.9, Java 21
* Spring Web, Spring Data JPA (Hibernate), Spring Security
* OAuth2 Client, JWT
* H2 인메모리 데이터베이스 (기본), PostgreSQL 드라이버 포함
* Thymeleaf, WebJars (Bootstrap, jQuery)
* SpringDoc OpenAPI

## 프로젝트 구조

헥사고날(포트-어댑터) 성격의 계층 구조로 구성되어 있습니다.

* `src/main/kotlin/com/elseeker`

    * `common`: 공통 인프라
        * `config`: 애플리케이션 설정 (Properties, JPA, Swagger)
        * `security`: Spring Security, JWT 필터, OAuth2 (팩토리/핸들러/서비스/유틸)
        * `domain`: 공통 엔티티(`BaseEntity`, `BaseTimeEntity`), `ServiceError`
        * `adapter/input/web`: 에러 핸들러(`GlobalExceptionHandler`), 루트 컨트롤러
        * `adapter/input/api`: 공통 API 응답 모델
        * `adapter/output/jpa`: 공통 JPA 확장
    * `auth`: 인증/인가 도메인
        * `adapter/input/api`: 인증 REST API (클라이언트/관리자)
        * `adapter/input/web`: 로그인/로그아웃 웹 컨트롤러
        * `adapter/output`: 토큰/쿠키 저장소
        * `application`: 인증 서비스
        * `domain`: 인증 도메인 모델
    * `bible`: 성경 도메인
        * `domain/model`: 엔티티 (`BibleTranslation`, `BibleChapter`, `BibleVerse` 등)
        * `domain/vo`: 값 객체 (`BibleBookKey`, `BibleTestamentType`, `BibleTranslationType`, `DirectionType`)
        * `domain/result`: 서비스 출력 DTO
        * `application/service`: 관리자/클라이언트 서비스
        * `application/component`: 도메인 로직 (`BibleReader`)
        * `adapter/input/api`: REST API (관리자 CRUD, 클라이언트 조회/검색/메모/하이라이트/읽기진행도)
        * `adapter/input/web`: 웹 컨트롤러 (관리자/클라이언트)
        * `adapter/output/jpa`: JPA 리포지토리
    * `study`: 스터디(사전/역사) 도메인
        * `domain/model`: 엔티티, `domain/vo`: 값 객체
        * `application/service`: 사전/역사 서비스
        * `application/component`: 도메인 로직
        * `adapter/input/api`: REST API (관리자/클라이언트)
        * `adapter/input/web`: 웹 컨트롤러 (관리자/클라이언트)
        * `adapter/output/jpa`: JPA 리포지토리
    * `community`: 커뮤니티 도메인
        * `domain/model`: 엔티티 (`Post`, `Comment`), `domain/vo`: 값 객체, `domain/policy`: 도메인 정책
        * `application/service`: 게시글/댓글/신고 서비스
        * `application/component`: 도메인 로직, `application/mapper`: DTO 매퍼
        * `adapter/input/api`: REST API (관리자/클라이언트)
        * `adapter/input/web`: 웹 컨트롤러 (관리자/클라이언트)
        * `adapter/output/jpa`: JPA 리포지토리 (Kotlin JDSL 포함)
    * `game`: 게임 도메인 (퀴즈/OX퀴즈/타자연습/뽑기/단어퍼즐)
        * `domain/model`: 엔티티 (`OxStage`, `OxQuestion`, `BibleTypingSession`, `BibleTypingVerse` 등)
        * `domain/vo`: 값 객체 (`QuizDifficulty`)
        * `application/service`: 게임별 서비스
        * `application/component`: 도메인 로직, `application/dto`: 내부 DTO, `application/mapper`: DTO 매퍼
        * `adapter/input/api`: REST API (관리자/클라이언트, 단어퍼즐 포함)
        * `adapter/input/web`: 웹 컨트롤러 (관리자/클라이언트)
        * `adapter/output/jpa`: JPA 리포지토리
    * `member`: 회원 도메인
        * `domain/model`: 엔티티, `domain/vo`: 값 객체 (`MemberRole`)
        * `application/service`: 회원 서비스
        * `adapter/input/api`: REST API (관리자/클라이언트)
        * `adapter/input/web`: 웹 컨트롤러 (관리자/클라이언트)
        * `adapter/output/jpa`: JPA 리포지토리
* `src/main/resources`

    * `application.yml`, `application-local.yml`, `application-prod.yml`: 프로필별 설정
    * `data/`: SQL 시드 데이터 (번역본, 성경 본문, 퀴즈, 사전, 단어 퍼즐)
    * `templates/`: Thymeleaf 템플릿 (69개 파일)
    * `static/`: CSS (38개), JavaScript (37개), 이미지/아이콘
* `src/test/kotlin/com/elseeker`

    * `common`: 테스트 인프라 (`IntegrationTest`, `DatabaseCleaner`, `TestContainers`, `TestProfileResolver`)
    * `game`: 게임 서비스 테스트
* `docs/`: 도메인별 문서 (ERD 등)

## 개발 가이드

* Commit 메시지는 AngularJS 컨벤션 prefix를 따릅니다: `feat:`, `fix:`, `docs:`, `style:`, `refactor:`, `test:`, `chore:` 등.
* Swagger/OpenAPI 어노테이션은 `*ApiDocument` 인터페이스에 작성하고, 컨트롤러는 해당 인터페이스를 구현합니다.
* 웹 UI의 뒤로가기 동작은 공통 네비게이션바의 백버튼(`topNavBackButton`)을 사용합니다. 커스텀 이동 경로가 필요하면 `<body>`에 `data-back-link`를 지정합니다.
* Hover 스타일은 데스크톱(마우스) 환경에서만 적용합니다. 모든 hover CSS는 `@media (hover: hover) and (pointer: fine)` 내부에 작성하고, 모바일/터치 UI에서는 hover 기반 UX를 설계하지 않습니다.
* **Active 메뉴 처리**: Thymeleaf 3.1부터 템플릿 내 `#request` 직접 접근이 차단되므로, `@ControllerAdvice` + `@ModelAttribute("currentPath")`로 서버에서 현재 경로를 주입합니다. 템플릿에서는 `th:classappend="${#strings.startsWith(currentPath, '/경로')} ? 'active'"` 패턴을 사용합니다. JS
  `location.pathname`으로 active 클래스를 토글하는 방식은 SSR 원칙 위배로 사용하지 않습니다. (참고: `GlobalModelAttribute.kt`)

## SEO 가이드

새 페이지를 추가하거나 기존 페이지를 수정할 때 아래 규칙을 따릅니다.

### 공통 head fragment

모든 페이지는 `fragments/head.html`의 공통 fragment를 사용합니다. fragment가 자동으로 처리하는 항목:

* `<title>` — 첫 번째 파라미터로 전달
* `<meta description>` — `pageDescription` 변수 (미설정 시 사이트 기본 설명 사용)
* `<link rel="canonical">` — 현재 요청 URI 기반 자동 생성
* Open Graph / Twitter Card 메타 태그 — title, description, image 자동 매핑
* JSON-LD 구조화 데이터 — Organization, WebSite, WebPage 3중 구조

### 새 페이지 추가 시 필수 작업

1. **`pageDescription` 설정** — 페이지 고유의 설명을 50~160자(한글) 이내로 작성합니다.
   ```html
   <head th:replace="~{fragments/head :: head('페이지명 | ElSeeker', true, '/css/feature.css')}"
         th:with="pageDescription='이 페이지의 고유한 설명을 작성합니다.'"></head>
   ```

2. **로그인 필수 페이지는 `noindex` 설정** — 크롤러가 접근할 수 없는 페이지는 반드시 noindex를 지정합니다.
   ```html
   <head th:replace="~{fragments/head :: head('제목 | ElSeeker', true, '/css/feature.css')}"
         th:with="robotsContent='noindex'"></head>
   ```

3. **sitemap.xml 업데이트** — 공개 페이지를 추가한 경우 `src/main/resources/static/sitemap.xml`에 URL을 추가합니다.

4. **SecurityConfig permitAll 확인** — 공개 페이지인 경우 `SecurityConfig.kt`의 `permitAll()` 규칙에 포함되는지 확인합니다.

### th:with 변수 목록

| 변수                | 용도                                            | 예시                                         |
|-------------------|-----------------------------------------------|--------------------------------------------|
| `pageDescription` | 페이지 meta description                          | `'성경 66권의 개요를 영상으로 학습합니다.'`                |
| `robotsContent`   | robots 메타 태그                                  | `'noindex'`                                |
| `schemaType`      | JSON-LD @type (기본: `WebPage`)                 | `'CollectionPage'`, `'Article'`            |
| `ogType`          | Open Graph type (기본: `website`)               | `'article'`                                |
| `ogImage`         | Open Graph 이미지 URL                            | `'https://elseeker.com/images/custom.png'` |
| `canonicalUrl`    | canonical URL 직접 지정                           | `'https://elseeker.com/web/bible/search'`  |
| `twitterCard`     | Twitter Card type (기본: `summary_large_image`) | `'summary'`                                |

복수 변수를 콤마로 조합할 수 있습니다:

```html
th:with="pageDescription='설명', schemaType='CollectionPage'"
```

### 페이지 분류별 SEO 정책

| 분류                       | pageDescription |    noindex     | sitemap 포함 |
|--------------------------|:---------------:|:--------------:|:----------:|
| 공개 페이지 (성경, 학습, 커뮤니티 목록) |       필수        |       X        |     O      |
| 로그인 필수 페이지 (게임, 마이페이지)   |       선택        |       O        |     X      |
| 관리자 페이지                  |       불필요       | robots.txt로 차단 |     X      |
| 에러 페이지                   |       불필요       |       O        |     X      |

### robots.txt 규칙

`src/main/resources/static/robots.txt`에서 크롤러 접근을 관리합니다:

* `/web/admin/` — 관리자 페이지 차단
* `/web/member/` — 회원 전용 페이지 차단
* `/web/auth/` — 인증 페이지 차단
* `/api/` — API 엔드포인트 차단
* `Sitemap` — sitemap.xml 위치 명시

## 로컬 실행 방법

```bash
./gradlew bootRun
```

* 애플리케이션 접속: `http://localhost:8080`
* Swagger UI: `http://localhost:8080/swagger-ui/index.html`
* H2 콘솔: `http://localhost:8080/h2-console`

    * JDBC URL: `jdbc:h2:mem:test`

## 빌드 및 테스트

```bash
./gradlew build
./gradlew test
./gradlew bootJar
```

## 시간대 정책

API는 모든 시간 값을 UTC 기준 ISO-8601 형식으로 응답합니다. 사용자 타임존에 따른 표시 변환은 클라이언트에서 수행합니다.
예: `2024-01-01T10:00:00Z`

## 인증 및 토큰 갱신

* OAuth2 로그인 성공 시 Access/Refresh JWT가 HttpOnly 쿠키로 발급됩니다.
* Access 토큰이 만료되면 서버 `JwtRefreshFilter`가 Refresh 토큰으로 자동 재발급을 시도합니다.
* 명시적 재발급은 `POST /api/v1/auth/refresh`로 수행합니다(실패 시 401).

## REST API 엔드포인트

아래는 주요 엔드포인트 요약이며, 전체 목록은 Swagger UI를 참고하세요.

```text
GET /api/v1/community/posts
GET /api/v1/community/posts/{postId}
POST /api/v1/community/posts
PUT /api/v1/community/posts/{postId}
DELETE /api/v1/community/posts/{postId}
POST /api/v1/community/posts/{postId}/reactions
DELETE /api/v1/community/posts/{postId}/reactions/{type}
GET /api/v1/community/posts/{postId}/comments
POST /api/v1/community/posts/{postId}/comments
PUT /api/v1/community/posts/{postId}/comments/{commentId}
DELETE /api/v1/community/posts/{postId}/comments/{commentId}
POST /api/v1/community/posts/{postId}/reports
POST /api/v1/community/posts/{postId}/comments/{commentId}/reports
GET /api/v1/community/posts/top
GET /api/v1/admin/community/posts
GET /api/v1/admin/community/posts/{postId}
POST /api/v1/admin/community/posts
PUT /api/v1/admin/community/posts/{postId}
DELETE /api/v1/admin/community/posts/{postId}
PATCH /api/v1/admin/community/posts/{postId}/status
GET /api/v1/admin/community/comments
PATCH /api/v1/admin/community/comments/{commentId}/status
DELETE /api/v1/admin/community/comments/{commentId}
POST /api/v1/admin/community/comments/{commentId}/restore
GET /api/v1/admin/community/reports
GET /api/v1/bibles/translations
GET /api/v1/bibles/translations/{translationId}/books
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/verses
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/navigate?direction=prev|next
GET /api/v1/bibles/translations/{translationId}/search?keyword={searchTerm}
GET /api/v1/bibles/daily?translationType=KRV|KJV|NKRV
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/memos
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/highlights
PUT /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/verses/{verseNumber}/memo
PUT /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/verses/{verseNumber}/highlight
DELETE /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/verses/{verseNumber}/memo
DELETE /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/verses/{verseNumber}/highlight
GET /api/v1/bible/translations
GET /api/v1/bible/books?translationId={translationId}
GET /api/v1/bible/chapters?translationId={translationId}&bookOrder={bookOrder}
GET /api/v1/bible/verses?translationId={translationId}&bookOrder={bookOrder}&chapterNumber={chapterNumber}
GET /api/v1/auth/me
POST /api/v1/auth/refresh
PUT /api/v1/members/{memberUid}
DELETE /api/v1/members/{memberUid}
GET /api/v1/members/{memberUid}/oauth-accounts
POST /api/v1/members/{memberUid}/oauth-accounts
DELETE /api/v1/members/{memberUid}/oauth-accounts?provider={provider}&providerUserId={providerUserId}
POST /api/v1/members/{memberUid}/oauth-accounts/initialize-profile
GET /api/v1/study/dictionaries
GET /api/v1/study/dictionaries/{id}
GET /api/v1/game/bible-quiz/stages
GET /api/v1/game/bible-quiz/stages/{stageNumber}
POST /api/v1/game/bible-quiz/stages/{stageNumber}/start
POST /api/v1/game/bible-quiz/stages/{stageNumber}/answer
POST /api/v1/game/bible-quiz/stages/{stageNumber}/complete
POST /api/v1/game/bible-quiz/progress/reset
GET /api/v1/game/bible-ox-quiz/stages
GET /api/v1/game/bible-ox-quiz/stages/{stageNumber}
POST /api/v1/game/bible-ox-quiz/stages/{stageNumber}/start
POST /api/v1/game/bible-ox-quiz/stages/{stageNumber}/questions/{questionId}/answer
POST /api/v1/game/bible-ox-quiz/stages/{stageNumber}/complete
GET /api/v1/game/bible-typing/sessions
POST /api/v1/game/bible-typing/sessions
POST /api/v1/game/bible-typing/sessions/{sessionKey}/end
DELETE /api/v1/game/bible-typing/sessions
GET /api/v1/game/bible-typing/progress
GET /api/v1/game/bible-typing/progress/latest
POST /api/v1/game/bible-typing/progress/verses
GET /api/v1/game/word-puzzles
POST /api/v1/game/word-puzzles/{puzzleId}/attempts
GET /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}
PATCH /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/cells
POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/hints/reveal-letter
POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/hints/check-word
POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/submit
POST /api/v1/bible/reading/chapters/read
GET /api/v1/bible/reading/chapters/read
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/state
```

### 관리자 API (요약)

* 관리자 API는 `/api/v1/admin/**` 경로에 있으며 `ADMIN` 권한이 필요합니다.
* 주요 영역: `admin/bible`, `admin/dictionaries`, `admin/word-puzzles`, `admin/members`, `admin/quiz`, `admin/ox`, `admin/community`
* 상세 엔드포인트는 Swagger UI를 참고하세요.

## 웹 UI 라우트

```text
GET /
GET /web/bible/translation
GET /web/bible/book
GET /web/bible/book/description
GET /web/bible/chapter
GET /web/bible/verse
GET /web/bible/search
GET /web/auth/login
GET /web/auth/logout
GET /web/legal/terms
GET /web/legal/privacy
GET /web/game
GET /web/game/bible-quiz
GET /web/game/bible-quiz/map
GET /web/game/bible-typing
GET /web/game/bible-ox-quiz
GET /web/game/bible-ox-quiz/map
GET /web/game/bible-casting-lots
GET /web/game/bible-word-puzzle
GET /web/game/bible-word-puzzle/play
GET /web/community
GET /web/community/write
GET /web/community/{postId}
GET /web/study
GET /web/study/bible-overview-video
GET /web/study/bible-genealogy
GET /web/study/twelve-disciples
GET /web/study/twelve-tribes
GET /web/study/lords-prayer
GET /web/study/apostles-creed
GET /web/study/history
GET /web/study/history/{era}
GET /web/study/history/event/{id}
GET /web/study/dictionary
GET /web/study/dictionary/{id}
GET /web/member/mypage
GET /web/member/withdraw
GET /web/member/withdraw/complete
GET /web/admin
GET /web/admin/community
GET /web/admin/community/posts
GET /web/admin/community/posts/new
GET /web/admin/community/posts/{postId}
GET /web/admin/community/posts/{postId}/edit
GET /web/admin/community/comments
GET /web/admin/community/reports
GET /web/admin/bible/translations
GET /web/admin/bible/translations/new
GET /web/admin/bible/translations/{id}/edit
GET /web/admin/bible/translations/{translationId}/books
GET /web/admin/bible/translations/{translationId}/books/new
GET /web/admin/bible/translations/{translationId}/books/{id}/edit
GET /web/admin/bible/book-descriptions
GET /web/admin/bible/book-descriptions/new
GET /web/admin/bible/book-descriptions/{id}/edit
GET /web/admin/bible/books/{bookId}/chapters
GET /web/admin/bible/books/{bookId}/chapters/new
GET /web/admin/bible/books/{bookId}/chapters/{id}/edit
GET /web/admin/bible/chapters/{chapterId}/verses
GET /web/admin/bible/chapters/{chapterId}/verses/new
GET /web/admin/bible/chapters/{chapterId}/verses/{id}/edit
GET /web/admin/dictionaries
GET /web/admin/dictionaries/new
GET /web/admin/dictionaries/{id}/edit
GET /web/admin/word-puzzles
GET /web/admin/word-puzzles/new
GET /web/admin/word-puzzles/{id}/edit
GET /web/admin/word-puzzles/{puzzleId}/entries
GET /web/admin/word-puzzles/{puzzleId}/entries/new
GET /web/admin/word-puzzles/{puzzleId}/entries/{entryId}/edit
GET /web/admin/members
GET /web/admin/members/{id}/edit
GET /web/admin/quiz/stages
GET /web/admin/quiz/stages/new
GET /web/admin/quiz/stages/{id}/edit
GET /web/admin/quiz/stages/{stageId}/questions
GET /web/admin/quiz/stages/{stageId}/questions/new
GET /web/admin/quiz/questions/{id}/edit
GET /web/admin/ox-quiz/stages
GET /web/admin/ox-quiz/stages/new
GET /web/admin/ox-quiz/stages/{id}/edit
GET /web/admin/ox-quiz/stages/{stageId}/questions
GET /web/admin/ox-quiz/stages/{stageId}/questions/new
GET /web/admin/ox-quiz/questions/{id}/edit
```

### 템플릿 매핑

`src/main/resources/templates` (69개 파일)

```text
index.html
error.html

bible/
  translation-list, book-list, book-description, chapter-list, verse-list, search

game/
  game, bible-quiz, bible-quiz-map, bible-ox-quiz, bible-ox-quiz-map,
  bible-typing, bible-casting-lots, bible-word-puzzle, bible-word-puzzle-play

study/
  study, bible-overview-video, bible-genealogy,
  twelve-disciples, twelve-tribes, lords-prayer, apostles-creed,
  history, history-era, history-event,
  dictionary-list, dictionary-detail

community/
  community, community-write, community-detail

member/
  mypage, withdraw, withdraw-complete

login/
  login

legal/
  terms, privacy

admin/
  admin-dashboard,
  admin-bible-translation-list, admin-bible-translation-form,
  admin-bible-book-list, admin-bible-book-form,
  admin-bible-book-description-list, admin-bible-book-description-form,
  admin-bible-chapter-list, admin-bible-chapter-form,
  admin-bible-verse-list, admin-bible-verse-form,
  admin-dictionary-list, admin-dictionary-form,
  admin-word-puzzle-list, admin-word-puzzle-form,
  admin-word-puzzle-entry-list, admin-word-puzzle-entry-form,
  admin-quiz-stage-list, admin-quiz-stage-form,
  admin-quiz-question-list, admin-quiz-question-form,
  admin-ox-stage-list, admin-ox-stage-form,
  admin-ox-question-list, admin-ox-question-form,
  admin-member-list, admin-member-form,
  admin-community-post-list, admin-community-post-form, admin-community-post-detail,
  admin-community-comment-list, admin-community-report-list

fragments/
  head, header, footer, community-widgets
  admin/admin-sidebar
```

## 데이터 로딩 방식

애플리케이션 시작 시 JPA가 테이블을 생성한 이후 `spring.sql.init` 설정을 통해 SQL 시드 데이터를 자동으로 로딩합니다. 시드 파일은 `src/main/resources/data` 경로에 위치하며 다음과 같은 파일로 구성되어 있습니다.

* `bible_translation.sql` — 번역본 정의
* `bible_book_description_ko.sql`, `bible_book_description_en.sql` — 책 소개 (한/영)
* `krv/bible_krv_book.sql` — KRV 책 목록
* `krv/bible_krv_01_genesis.sql` ~ `krv/bible_krv_66_revelation.sql` — KRV 66권 전체 본문
* `nkrv/bible_nkrv_book.sql` — NKRV 책 목록
* `nkrv/bible_nkrv_01_genesis.sql`, `nkrv/bible_nkrv_02_exodus.sql` — NKRV 창세기/출애굽기
* `kjv/bible_kjv_book.sql` — KJV 책 목록 (본문 미포함)
* `bible_quiz.sql` — 성경 퀴즈 데이터
* `quiz_ox_quiz.sql` — OX 퀴즈 데이터
* `word_puzzle_step1.sql`, `word_puzzle_step2.sql` — 단어 퍼즐 데이터
* `dictionary.sql` — 성경 사전 데이터

현재 시드 데이터는 개역한글(KRV) 66권 전체 본문을 포함합니다. 새번역(NKRV)은 창세기/출애굽기만, KJV는 책 목록만 제공됩니다.
