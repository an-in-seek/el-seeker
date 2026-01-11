# Repository Guidelines

## Project Structure & Module Organization
- `src/main/kotlin/com/elseeker`: Spring Boot app code organized by domain modules.
  - `common`: shared config, error model, common web components.
  - `bible`: Bible domain (domain, application, adapter/in, adapter/out).
  - `study`: Study domain (dictionary/history) with the same adapter structure.
- `src/main/resources`: configuration and assets.
  - `application.yml`: runtime config (H2, JPA).
  - `data/`: SQL seed scripts for translations/books/chapters/verses and dictionary.
  - `templates/`: Thymeleaf HTML pages.
  - `static/`: CSS, JS, images.
- No `src/test` directory yet; add tests under `src/test/kotlin` when introducing them.

## Build, Test, and Development Commands
- `./gradlew bootRun`: run the service locally with the embedded H2 database.
- `./gradlew build`: compile and run tests (if present), produce the boot JAR.
- `./gradlew test`: run unit tests with JUnit 5.
- `./gradlew bootJar`: build the runnable Spring Boot artifact.

## Coding Style & Naming Conventions
- Kotlin + Spring Boot 3; keep idiomatic Kotlin (data classes, null-safety) and Spring annotations.
- Use 4-space indentation and standard Kotlin naming: `UpperCamel` for classes, `lowerCamel` for functions/vars, `UPPER_SNAKE` for constants.
- SQL seed files follow `bible_krv_XX_<book>.sql` in `src/main/resources/data/krv`.
- No formatter or linter is configured; avoid reformatting unrelated files.
- Frontend JS: prefer ES module scripts (`type="module"`); avoid IIFEs and `'use strict'` in module files. Use explicit `import`/`export` instead of globals.

## Testing Guidelines
- Tests should use `spring-boot-starter-test` with JUnit 5.
- Place unit tests under `src/test/kotlin` mirroring production packages.
- Name tests `*Test.kt` and focus on service, repository, and controller behavior.

## Commit & Pull Request Guidelines
- Commit messages follow a prefix style like `feat:` or `doc:` (see `git log -5`).
- Keep commits scoped to one feature/fix; include a short, imperative summary.
- PRs should describe the change, link related issues, and include screenshots for UI/template changes.

## Configuration & Data Notes
- H2 runs in-memory by default (`jdbc:h2:mem:test`); data is loaded from SQL in `src/main/resources/data`.
- If you add new Bible content, keep book order and naming consistent with existing SQL files.
