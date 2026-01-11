# Repository Guidelines (Gemini Code Assist)

## Project Structure & Modules
- `src/main/kotlin/com/elseeker`: Spring Boot 3 app code by domain modules.
  - `common`: shared config, errors, shared web components.
  - `bible`: Bible domain (domain, application, adapter/in, adapter/out).
  - `study`: Study domain (dictionary/history) with same adapter structure.
- `src/main/resources`:
  - `application.yml`: runtime config (H2, JPA).
  - `data/`: SQL seed scripts for translations/books/chapters/verses and dictionary.
  - `templates/`: Thymeleaf HTML pages.
  - `static/`: CSS, JS, images.
- Tests live under `src/test/kotlin` (none yet).

## Build, Test, Run
- `./gradlew bootRun`: run locally with embedded H2.
- `./gradlew build`: compile + tests (if present), build boot JAR.
- `./gradlew test`: run JUnit 5 tests.
- `./gradlew bootJar`: build runnable artifact.

## Coding Style
- Kotlin idioms: data classes, null-safety, Spring annotations.
- 4-space indentation; Kotlin naming: `UpperCamel` classes, `lowerCamel` vars/functions, `UPPER_SNAKE` constants.
- No formatter configured; avoid reformatting unrelated files.

## Frontend JS Conventions
- Prefer ES modules (`type="module"`).
- Avoid IIFEs and `'use strict'` in module files (modules are strict by default).
- Use explicit `import`/`export`; avoid globals.

## Testing
- Use `spring-boot-starter-test` with JUnit 5.
- Mirror production packages under `src/test/kotlin`.
- Name tests `*Test.kt`; focus on service/repository/controller behavior.

## Data & SQL
- SQL seed files follow `bible_krv_XX_<book>.sql` in `src/main/resources/data/krv`.
- Keep Bible book order and naming consistent with existing files.

## Commits & PRs
- Commit messages use prefixes like `feat:` or `doc:` (see `git log -5`).
- Keep commits scoped to one feature/fix.
- PRs should describe changes, link issues, and include screenshots for UI/template changes.
