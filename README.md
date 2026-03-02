# ElSeeker

ElSeeker는 "하나님을 구하는 사람" 또는 "하나님을 찾는 사람"이라는 의미를 지닌 성경 플랫폼 서비스입니다. Kotlin과 Spring Boot 기반으로 개발되었으며 REST API와 Thymeleaf 기반 웹 UI를 통해 성경 번역본, 책, 장, 절 데이터를
조회하고 검색할 수 있습니다.
성경 퀴즈 스테이지와 스터디(사전/역사) 콘텐츠를 제공하고, 기본은 인메모리 H2 데이터베이스를 사용하며 애플리케이션 시작 시 SQL 시드 파일로 초기 데이터를 로딩합니다.

## 브랜드/철학

ElSeeker는 "하나님을 구하는 사람/하나님을 찾는 사람"이라는 의미를 바탕으로, 성경을 통해 하나님을 알아가는 여정을 돕는 플랫폼을 지향합니다.

## 주요 기능

* 성경 번역본, 책, 장, 절 조회 REST API 제공
* 특정 번역본 내 성경 구절 키워드 검색 기능 제공
* 오늘의 말씀(일일 구절) 조회 REST API 제공
* 성경 구절 메모 조회/등록/삭제 REST API 제공
* Thymeleaf 기반 성경 탐색 및 검색 웹 UI 제공
* **커뮤니티(게시글/댓글/리액션/신고)** 기능 제공
* **관리자 커뮤니티**: 공지/게시글/댓글/신고 관리 화면 제공
* 성경 퀴즈 스테이지 조회 REST API 및 웹 UI 제공
* **성경 OX 퀴즈** 스테이지/문항 진행 및 결과 제공
* **성경 타자 연습(Bible Typing)** 기능 제공
    * 번역본/책/장 단위 타자 연습 세션 생성 및 진행
    * 연습 진행도 저장 및 조회
    * 타자 정확도 및 속도 측정
* **성경 개요 영상(Bible Overview Video)** 66권 유튜브 영상 목록 웹 UI 제공
* **성경 족보(Bible Genealogy)** 마태복음/누가복음 족보 비교 웹 UI 제공
* 스터디(사전/역사) 조회 REST API 및 웹 UI 제공
* OAuth2 로그인/로그아웃 및 회원 정보 관리 API 제공
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

    * `common`: 공통 설정, 에러 모델, 공용 웹 구성
    * `auth`: 인증/인가, OAuth2, JWT
    * `bible`: 성경 도메인
        * `domain`: 도메인 모델/값 객체/결과 모델
        * `application`: 유스케이스 및 서비스/컴포넌트
        * `adapter/input`: REST API, Web Controller
        * `adapter/output`: JPA 리포지토리
    * `study`: 스터디(사전/역사) 도메인
        * `domain`: 도메인 모델
        * `application`: 애플리케이션 서비스
        * `adapter/input`: REST API, Web Controller
        * `adapter/output`: JPA 리포지토리
    * `community`: 커뮤니티 도메인
        * `domain`: 도메인 모델/값 객체
        * `application`: 서비스/정책/매퍼
        * `adapter/input`: REST API, Web Controller
        * `adapter/output`: JPA 리포지토리
    * `game`: 성경 퀴즈 도메인
        * `domain`: 도메인 모델
        * `application`: 애플리케이션 서비스/매퍼
        * `adapter/input`: REST API, Web Controller
        * `adapter/output`: JPA 리포지토리
    * `member`: 회원 도메인
* `src/main/resources`

    * `application.yml`: 애플리케이션 설정
    * `data/`: SQL 시드 데이터
    * `templates/`: Thymeleaf 템플릿
    * `static/`: CSS, JavaScript, 이미지 리소스
* `docs/`: 도메인별 문서

## 개발 가이드

* Commit 메시지는 AngularJS 컨벤션 prefix를 따릅니다: `feat:`, `fix:`, `docs:`, `style:`, `refactor:`, `test:`, `chore:` 등.
* Swagger/OpenAPI 어노테이션은 `*ApiDocument` 인터페이스에 작성하고, 컨트롤러는 해당 인터페이스를 구현합니다.
* 웹 UI의 뒤로가기 동작은 공통 네비게이션바의 백버튼(`topNavBackButton`)을 사용합니다. 커스텀 이동 경로가 필요하면 `<body>`에 `data-back-link`를 지정합니다.
* Hover 스타일은 데스크톱(마우스) 환경에서만 적용합니다. 모든 hover CSS는 `@media (hover: hover) and (pointer: fine)` 내부에 작성하고, 모바일/터치 UI에서는 hover 기반 UX를 설계하지 않습니다.

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
```

### 관리자 API (요약)

* 관리자 API는 `/api/v1/admin/**` 경로에 있으며 `ADMIN` 권한이 필요합니다.
* 주요 영역: `admin/bible`, `admin/dictionaries`, `admin/members`, `admin/quiz`, `admin/ox`, `admin/community`
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
GET /web/community
GET /web/community/write
GET /web/community/{postId}
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
GET /web/admin/bible/books/{bookId}/chapters
GET /web/admin/bible/books/{bookId}/chapters/new
GET /web/admin/bible/books/{bookId}/chapters/{id}/edit
GET /web/admin/bible/chapters/{chapterId}/verses
GET /web/admin/bible/chapters/{chapterId}/verses/new
GET /web/admin/bible/chapters/{chapterId}/verses/{id}/edit
GET /web/admin/dictionaries
GET /web/admin/dictionaries/new
GET /web/admin/dictionaries/{id}/edit
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
GET /web/member/mypage
GET /web/member/withdraw
GET /web/member/withdraw/complete
GET /web/study
GET /web/study/bible-overview-video
GET /web/study/bible-genealogy
GET /web/study/history
GET /web/study/history/{era}
GET /web/study/history/event/{id}
GET /web/study/dictionary
GET /web/study/dictionary/{id}
```

### 템플릿 매핑

`src/main/resources/templates`

```text
index -> index.html
bible/search -> bible/search.html
bible/translation-list -> bible/translation-list.html
bible/book-list -> bible/book-list.html
bible/book-description -> bible/book-description.html
bible/chapter-list -> bible/chapter-list.html
bible/verse-list -> bible/verse-list.html
community/community -> community/community.html
community/community-write -> community/community-write.html
community/community-detail -> community/community-detail.html
admin/admin-dashboard -> admin/admin-dashboard.html
admin/admin-bible-translation-list -> admin/admin-bible-translation-list.html
admin/admin-bible-translation-form -> admin/admin-bible-translation-form.html
admin/admin-bible-book-list -> admin/admin-bible-book-list.html
admin/admin-bible-book-form -> admin/admin-bible-book-form.html
admin/admin-bible-chapter-list -> admin/admin-bible-chapter-list.html
admin/admin-bible-chapter-form -> admin/admin-bible-chapter-form.html
admin/admin-bible-verse-list -> admin/admin-bible-verse-list.html
admin/admin-bible-verse-form -> admin/admin-bible-verse-form.html
admin/admin-dictionary-list -> admin/admin-dictionary-list.html
admin/admin-dictionary-form -> admin/admin-dictionary-form.html
admin/admin-member-list -> admin/admin-member-list.html
admin/admin-member-form -> admin/admin-member-form.html
admin/admin-community-post-list -> admin/admin-community-post-list.html
admin/admin-community-post-form -> admin/admin-community-post-form.html
admin/admin-community-post-detail -> admin/admin-community-post-detail.html
admin/admin-community-comment-list -> admin/admin-community-comment-list.html
admin/admin-community-report-list -> admin/admin-community-report-list.html
admin/admin-quiz-stage-list -> admin/admin-quiz-stage-list.html
admin/admin-quiz-stage-form -> admin/admin-quiz-stage-form.html
admin/admin-quiz-question-list -> admin/admin-quiz-question-list.html
admin/admin-quiz-question-form -> admin/admin-quiz-question-form.html
admin/admin-ox-stage-list -> admin/admin-ox-stage-list.html
admin/admin-ox-stage-form -> admin/admin-ox-stage-form.html
admin/admin-ox-question-list -> admin/admin-ox-question-list.html
admin/admin-ox-question-form -> admin/admin-ox-question-form.html
game/game -> game/game.html
game/bible-quiz -> game/bible-quiz.html
game/bible-quiz-map -> game/bible-quiz-map.html
game/bible-typing -> game/bible-typing.html
game/bible-ox-quiz -> game/bible-ox-quiz.html
game/bible-ox-quiz-map -> game/bible-ox-quiz-map.html
login/login -> login/login.html
member/mypage -> member/mypage.html
member/withdraw -> member/withdraw.html
member/withdraw-complete -> member/withdraw-complete.html
legal/terms -> legal/terms.html
legal/privacy -> legal/privacy.html
study/study -> study/study.html
study/bible-overview-video -> study/bible-overview-video.html
study/bible-genealogy -> study/bible-genealogy.html
study/history -> study/history.html
study/history-era -> study/history-era.html
study/history-event -> study/history-event.html
study/dictionary-list -> study/dictionary-list.html
study/dictionary-detail -> study/dictionary-detail.html
error -> error.html
```

## 데이터 로딩 방식

애플리케이션 시작 시 JPA가 테이블을 생성한 이후 `spring.sql.init` 설정을 통해 SQL 시드 데이터를 자동으로 로딩합니다. 시드 파일은 `src/main/resources/data` 경로에 위치하며 다음과 같은 파일로 구성되어 있습니다.

* `bible_translation.sql`
* `bible_book_description_ko.sql`
* `bible_book_description_en.sql`
* `bible_quiz.sql`
* `krv/bible_krv_book.sql`
* `nkrv/bible_nkrv_book.sql`
* `kjv/bible_kjv_book.sql`
* `bible_krv_XX_<book>.sql`
* `dictionary.sql`

현재 시드 데이터는 개역한글(KRV) 66권 전체 본문을 포함합니다. 번역본 목록에는 KRV/KJV가 포함되고 KJV는 책 목록만 제공됩니다.
