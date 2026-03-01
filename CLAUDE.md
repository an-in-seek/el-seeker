# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
./gradlew bootRun          # Run locally (H2 in-memory, local profile)
./gradlew build            # Build + run all tests
./gradlew test             # Run tests only
./gradlew test --tests "com.elseeker.game.application.service.BibleTypingSessionServiceTest"  # Single test class
./gradlew bootJar          # Produce runnable JAR
```

- Gradle 8.12.1, Java 21 toolchain, Kotlin 1.9.25, Spring Boot 3.5.9
- No linter or formatter configured
- **프론트엔드 전용 변경(HTML, CSS, JS, Thymeleaf 템플릿)만 수행한 경우 `./gradlew build` 또는 `./gradlew test`를 실행하지 않는다.** Kotlin/Java 코드 변경이 포함된 경우에만 빌드/테스트를 실행한다.

## Architecture

Hexagonal (ports & adapters) organized by domain module under `src/main/kotlin/com/elseeker`:

```
{module}/
  adapter/input/api/     — REST controllers (@RestController), *ApiDocument interfaces for Swagger
  adapter/input/web/     — Thymeleaf view controllers (@Controller)
  adapter/output/jpa/    — Spring Data JPA repositories with custom JPQL
  application/service/   — Thin service facades
  application/component/ — Domain logic helpers (e.g., BibleReader)
  domain/model/          — JPA @Entity classes
  domain/vo/             — Enums and value objects
  domain/result/         — Service output DTOs
```

Modules: `bible`, `study`, `game`, `member`, `auth`, `common`

### Key patterns
- Services delegate to `@Component` helpers for actual logic (e.g., `BibleService` → `BibleReader`)
- Swagger annotations live in `*ApiDocument` interfaces; controllers implement them
- Repositories use explicit `JOIN FETCH` in JPQL to prevent N+1 (entities default to `LAZY`)
- Error handling: `ServiceError` exception → `GlobalExceptionHandler` → `ErrorResponse`
- `BaseEntity` (`@MappedSuperclass`, `@Id IDENTITY`) is the root for all entities; `BaseTimeEntity` adds `@CreatedDate`/`@LastModifiedDate`

## Frontend

- Thymeleaf templates in `src/main/resources/templates/` using fragments (`fragments/head.html`, `fragments/header.html`)
- JavaScript: ES6 modules (`type="module"`), no bundler. Shared utils in `/js/storage-util.js` and `/js/common-util.js`
- CSS: Bootstrap 5.3 via WebJars + feature-specific CSS files. BEM-like naming per feature (e.g., `genealogy-node`, `bible-overview-video-card`)
- Hover styles: desktop-only via `@media (hover: hover) and (pointer: fine)`. Avoid hover-based UX for touch/mobile.
- Client-only pages (no server API): `bible-overview-video`, `bible-genealogy` — data is static JS arrays
- **CSS/JS 파일을 수정한 경우, 해당 파일을 참조하는 HTML 템플릿의 쿼리 파라미터 버전(`?v=`)을 반드시 올린다.** 예: `lords-prayer.css?v=1.0` → `lords-prayer.css?v=1.1`. 브라우저 캐시 무효화를 위해 필수.

## Auth & Security

- JWT (JJWT 0.12.3) + OAuth2 (Google, Naver, Kakao)
- Stateless sessions; Access/Refresh tokens in HttpOnly cookies
- Filter chain: `JwtRefreshFilter` → `JwtAuthenticationFilter` → Spring Security
- API auth failures → 401 JSON; Web auth failures → redirect to `/web/auth/login?returnUrl=...`
- Client-side: `fetchWithAuthRetry()` in `common-util.js` handles token refresh

## Database & Seed Data

- H2 in-memory (local/test), PostgreSQL 17 (prod)
- Schema: `spring.jpa.hibernate.ddl-auto: create` (local), `none` (prod)
- Seed data: `spring.sql.init` with `defer-datasource-initialization: true` loads SQL files from `src/main/resources/data/` after JPA schema creation
- Translations: KRV (66 books full text), NKRV (Genesis/Exodus only), KJV (book list only)

## Testing

- JUnit 5 + Kotest 5.9.1 + Testcontainers (PostgreSQL 17)
- `IntegrationTest` base class: auto-configures Testcontainers, creates test member in `@BeforeEach`, truncates all tables via `DatabaseCleaner` in `@AfterEach`
- `TestProfileResolver` forces `test` profile
- Test config: `src/test/resources/application-test.yml` (no seed data, `ddl-auto: update`)

## Git Commit Convention

- Commit 메시지는 AngularJS 컨벤션 prefix를 따른다: `feat:`, `fix:`, `docs:`, `style:`, `refactor:`, `test:`, `chore:` 등.
- 예: `feat: 성경 단어 퍼즐 기능 구현`, `fix: 퍼즐 보드 셀 렌더링 오류 수정`

## Environment Variables (prod)

`JWT_SECRET_BASE64`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `GOOGLE_CLIENT_ID/SECRET`, `NAVER_CLIENT_ID/SECRET`, `KAKAO_CLIENT_ID/SECRET`, `EL_SEEKER_API_BASE_URL`
