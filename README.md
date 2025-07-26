# 📖 The Bible Service

## 📌 프로젝트 개요

**The Bible Service**는 성경 데이터(번역본, 책, 장, 절)를 효율적으로 관리하고 제공하는 **RESTful API 및 웹 UI**를 포함한 서비스입니다.
Spring Boot와 Kotlin 기반으로 설계되었으며, **Domain Model Pattern** 원칙에 따라 도메인 중심의 구조를 갖추고 있습니다.

해당 프로젝트는 아래와 같은 기능을 제공합니다:

### 🔹 **1. RESTful API**

* 번역본, 책, 장, 절 정보를 계층적으로 탐색할 수 있는 API 제공
* 키워드 기반의 성경 구절 검색 기능 포함
* 클라이언트 앱, 웹 프론트엔드 등 다양한 외부 시스템과의 연동을 지원

### 🔹 **2. Thymeleaf 기반 웹 UI**

* 사용자가 웹 인터페이스를 통해 번역본, 책, 장, 절을 탐색 가능
* 키워드 검색을 통해 원하는 성경 구절을 빠르게 찾을 수 있음
* 서버 사이드 렌더링 기반의 HTML 템플릿 사용 (`Thymeleaf`)
* 프론트엔드에서는 **Bootstrap** 및 **JavaScript**를 활용하여 동적 기능 제공

## 📌 기술 스택

- **Backend**: Spring Boot 3.x, Kotlin, JPA (Hibernate)
- **Database**: H2
- **Build**: Gradle
- **Caching**: Redis (선택사항)
- **API 문서화**: SpringDoc OpenAPI (Swagger)
- **Web UI**: HTML, CSS, JavaScript, Thymeleaf, Bootstrap

---

## 📌 API 엔드포인트

### ✅ 번역본 리스트 조회

```
GET /api/bibles/translations
```

### ✅ 특정 번역본의 책 목록 조회

```
GET /api/bibles/translations/{translationId}/books
```

### ✅ 특정 책의 장 목록 및 기본 정보 조회

```
GET /api/bibles/translations/{translationId}/books/{bookOrder}/chapters
```

* `bookOrder`: 성경 내 정렬 순서

### ✅ 특정 장의 절 목록 조회

```
GET /api/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/verses
```

### ✅ 이전/다음 장으로 탐색

```
GET /api/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/navigate?direction=prev|next
```

* `direction`: `prev` 또는 `next`

### ✅ 특정 번역본 내 키워드로 성경 구절 검색

```
GET /api/bibles/translations/{translationId}/search?keyword={searchTerm}
```

📌 **Request Example**

```http
GET /api/bibles/translations/1/search?keyword=Jesus
```

📌 **Response Example**

```json
[
  {
    "verseId": 1,
    "bookName": "Matthew",
    "chapterNumber": 1,
    "verseNumber": 21,
    "text": "And she shall bring forth a son, and thou shalt call his name Jesus..."
  },
  ...
]
```

---

## ✅ **Thymeleaf 기반 성경 탐색 웹 UI**

웹 인터페이스를 통해 성경 번역본, 책, 장, 절을 순차적으로 탐색하거나 구절을 검색할 수 있습니다.
웹 페이지는 **Thymeleaf 템플릿 엔진**을 사용해 서버 사이드에서 렌더링됩니다.

### 📌 웹 UI 기능 및 경로

| 기능           | 경로                       | 설명                           |
|--------------|--------------------------|------------------------------|
| 번역본 목록 조회    | `/web/bible/translation` | 성경 번역본 리스트를 조회하여 화면에 렌더링     |
| 책 목록 페이지 진입  | `/web/bible/book`        | 선택한 번역본에 따라 성경 책 정보를 보여줄 페이지 |
| 장 목록 페이지 진입  | `/web/bible/chapter`     | 선택한 책에 따라 장 정보를 보여줄 페이지      |
| 절 목록 페이지 진입  | `/web/bible/verse`       | 선택한 장에 따라 절 정보를 보여줄 페이지      |
| 구절 검색 페이지 진입 | `/web/bible/search`      | 키워드 기반 성경 구절 검색 페이지 렌더링      |

---

### 📌 사용 기술

* **Thymeleaf**: 서버 사이드 템플릿 엔진
* **Bootstrap**: 반응형 웹 디자인을 위한 CSS 프레임워크
* **JavaScript**: DOM 조작 및 fetch 요청 등 동적 기능 구현

---

### 📁 템플릿 파일 이름 매핑

컨트롤러의 `return` 값은 템플릿 파일 이름과 매핑됩니다. 예:

| 리턴 값             | 템플릿 파일 경로 (`resources/templates`) |
|------------------|-----------------------------------|
| `"translations"` | `translations.html`               |
| `"books"`        | `books.html`                      |
| `"chapters"`     | `chapters.html`                   |
| `"verses"`       | `verses.html`                     |
| `"search"`       | `search.html`                     |

---

## 📌 데이터베이스 (H2 개발 환경)

### **H2 설정 (`application.yml`)**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
```

---

## 📌 도메인 모델

### **📍 도메인 관계도**

```
BibleTranslation (1) ───> (N) BibleBook
BibleBook (1) ───> (N) BibleChapter
BibleChapter (1) ───> (N) BibleVerse
```

- **BibleTranslation** → **BibleBook** (번역본 - 책, 1:N 관계)
- **BibleBook** → **BibleChapter** (책 - 장, 1:N 관계)
- **BibleChapter** → **BibleVerse** (장 - 절, 1:N 관계)

---

### **DDL 테이블 스키마 (자동 생성)**

### **bible_translation (번역본 테이블)**

| 컬럼명  | 타입           | 설명                  |
|------|--------------|---------------------|
| id   | BIGINT (PK)  | 번역본 ID (자동 증가)      |
| type | VARCHAR(50)  | 번역본 타입 (KRV, NIV 등) |
| name | VARCHAR(255) | 번역본 이름              |

### **bible_book (성경 책 테이블)**

| 컬럼명            | 타입           | 설명                            |
|----------------|--------------|-------------------------------|
| id             | BIGINT (PK)  | 책 ID (자동 증가)                  |
| translation_id | BIGINT (FK)  | 번역본 ID (bible_translation 연결) |
| name           | VARCHAR(255) | 책 이름 (예: 창세기, 마태복음)           |
| abbreviation   | VARCHAR(50)  | 책 약어 (예: 창, 마)                |
| testament_type | VARCHAR(10)  | 구약/신약 구분 (OLD, NEW)           |
| book_order     | INT          | 성경 내 정렬 순서                    |

### **bible_chapter (성경 장 테이블)**

| 컬럼명            | 타입          | 설명                   |
|----------------|-------------|----------------------|
| id             | BIGINT (PK) | 장 ID (자동 증가)         |
| book_id        | BIGINT (FK) | 책 ID (bible_book 연결) |
| chapter_number | INT         | 장 번호 (예: 1, 2, 3장)   |

### **bible_verse (성경 절 테이블)**

| 컬럼명          | 타입          | 설명                      |
|--------------|-------------|-------------------------|
| id           | BIGINT (PK) | 절 ID (자동 증가)            |
| chapter_id   | BIGINT (FK) | 장 ID (bible_chapter 연결) |
| verse_number | INT         | 절 번호 (예: 1, 2, 3절)      |
| text         | TEXT        | 성경 구절 내용                |

---

## 📌 실행 방법

```bash
# 프로젝트 빌드 및 실행
./gradlew bootRun
```
