# Bible Service

Bible Service는 Kotlin과 Spring Boot 기반으로 개발한 성경 데이터 제공 애플리케이션입니다. REST API와 Thymeleaf 기반 웹 UI를 통해 성경 번역본, 책, 장, 절 데이터를 조회하고 검색할 수 있습니다. 인메모리 H2 데이터베이스를
사용하며, 애플리케이션 시작 시 SQL 시드 파일을 통해 초기 데이터를 로딩합니다.

## 주요 기능

* 성경 번역본, 책, 장, 절 조회 REST API 제공
* 특정 번역본 내 성경 구절 키워드 검색 기능 제공
* Thymeleaf 기반 성경 탐색 및 검색 웹 UI 제공
* SpringDoc 기반 OpenAPI 및 Swagger UI 제공

## 기술 스택

* Kotlin 1.9.25, Spring Boot 3.4.3, Java 21
* Spring Web, Spring Data JPA (Hibernate)
* H2 인메모리 데이터베이스
* Thymeleaf, WebJars (Bootstrap, jQuery)
* SpringDoc OpenAPI

## 프로젝트 구조

헥사고날(포트-어댑터) 성격의 계층 구조로 구성되어 있습니다.

* `src/main/kotlin/com/elseeker`

    * `common`: 공통 설정, 에러 모델, 공용 웹 구성
    * `bible`: 성경 도메인
        * `domain`: 도메인 모델/값 객체/결과 모델
        * `application`: 유스케이스 및 서비스/컴포넌트
        * `adapter/in`: REST API, Web Controller
        * `adapter/out`: JPA 리포지토리
    * `study`: 스터디(사전/역사) 도메인
        * `domain`: 도메인 모델
        * `application`: 애플리케이션 서비스
        * `adapter/in`: REST API, Web Controller
        * `adapter/out`: JPA 리포지토리
* `src/main/resources`

    * `application.yml`: 애플리케이션 설정
    * `data/`: SQL 시드 데이터
    * `templates/`: Thymeleaf 템플릿
    * `static/`: CSS, JavaScript, 이미지 리소스

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

## REST API 엔드포인트

```text
GET /api/v1/bibles/translations
GET /api/v1/bibles/translations/{translationId}/books
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/verses
GET /api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/navigate?direction=prev|next
GET /api/v1/bibles/translations/{translationId}/search?keyword={searchTerm}
GET /api/v1/study/dictionaries
GET /api/v1/study/dictionaries/{id}
```

## 웹 UI 라우트

```text
GET /
GET /web/bible/translation
GET /web/bible/book
GET /web/bible/book/description
GET /web/bible/chapter
GET /web/bible/verse
GET /web/bible/search
GET /web/study
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
bible/translation-list -> bible/translation-list.html
bible/book-list -> bible/book-list.html
bible/book-description -> bible/book-description.html
bible/chapter-list -> bible/chapter-list.html
bible/verse-list -> bible/verse-list.html
bible/search -> bible/search.html
study/study -> study/study.html
study/history-main -> study/history-main.html
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
* `krv/bible_krv_book.sql`
* `nkrv/bible_nkrv_book.sql`
* `kjv/bible_kjv_book.sql`
* `bible_krv_XX_<book>.sql`
* `dictionary.sql`

현재 시드 데이터는 개역한글(KRV) 66권 전체 본문을 포함하며, 번역본 목록에는 KRV/NKRV/KJV가 포함됩니다(본문은 KRV만 제공).
