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

* `src/main/kotlin/com/elseeker/bible`

    * `domain`: 도메인 모델 및 핵심 비즈니스 로직
    * `application`: 유스케이스 및 애플리케이션 서비스
    * `infrastructure`: JPA, DB 접근 및 외부 연동
    * `presentation`: REST API 및 Web Controller
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
GET /api/bibles/translations
GET /api/bibles/translations/{translationId}/books
GET /api/bibles/translations/{translationId}/books/{bookOrder}
GET /api/bibles/translations/{translationId}/books/{bookOrder}/chapters
GET /api/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/verses
GET /api/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/navigate?direction=prev|next
GET /api/bibles/translations/{translationId}/search?keyword={searchTerm}
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
```

### 템플릿 매핑

`src/main/resources/templates`

```text
index -> index.html
translation -> translation.html
book -> book.html
book-description -> book-description.html
chapter -> chapter.html
verse -> verse.html
search -> search.html
error -> error.html
```

## 데이터 로딩 방식

애플리케이션 시작 시 JPA가 테이블을 생성한 이후 `spring.sql.init` 설정을 통해 SQL 시드 데이터를 자동으로 로딩합니다. 시드 파일은 `src/main/resources/data` 경로에 위치하며 다음과 같은 파일로 구성되어 있습니다.

* `bible_translation.sql`
* `bible_book.sql`
* `bible_krv_XX_<book>.sql`

현재 시드 데이터는 개역한글(KRV) 기준으로 창세기부터 로마서까지의 성경 데이터를 포함합니다.
