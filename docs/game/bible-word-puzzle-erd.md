```mermaid
erDiagram

  MEMBER ||--o{ WORD_PUZZLE_ATTEMPT : plays
  MEMBER ||--o{ MEMBER_DICTIONARY_PROGRESS : tracks
  DICTIONARY ||--o{ DICTIONARY_REFERENCE : has
  DICTIONARY ||--o{ WORD_PUZZLE_ENTRY : used_in
  DICTIONARY ||--o{ MEMBER_DICTIONARY_PROGRESS : progresses

  WORD_PUZZLE ||--o{ WORD_PUZZLE_ENTRY : contains
  WORD_PUZZLE ||--o{ WORD_PUZZLE_ATTEMPT : attempted_by

  WORD_PUZZLE_ATTEMPT ||--o{ WORD_PUZZLE_ATTEMPT_CELL : saves
  WORD_PUZZLE_ATTEMPT ||--o{ WORD_PUZZLE_HINT_USAGE : uses
  WORD_PUZZLE_ENTRY ||--o{ WORD_PUZZLE_HINT_USAGE : hints_for



  MEMBER {
    bigint id PK
    uuid uid UK
    citext email UK
    varchar nickname
    varchar member_role
    varchar profile_image_url
    timestamptz created_at
    timestamptz updated_at
  }



  DICTIONARY {
    bigint id PK
    varchar term
    text description
    text related_verses "기존 호환용, 점진적 마이그레이션"
    varchar original_language_code "nullable, HEBREW / GREEK"
    varchar original_lexeme "nullable"
    int bible_usage_count
    timestamptz created_at
    timestamptz updated_at
  }



  DICTIONARY_REFERENCE {
    bigint id PK
    bigint dictionary_id FK
    varchar verse_reference
    text verse_excerpt
    int display_order
    timestamptz created_at
  }



  WORD_PUZZLE {
    bigint id PK
    varchar title
    varchar theme_code
    varchar difficulty_code
    int board_width
    int board_height
    varchar puzzle_status_code "DRAFT / PUBLISHED / ARCHIVED"
    timestamptz published_at
    timestamptz created_at
    timestamptz updated_at
  }



  WORD_PUZZLE_ENTRY {
    bigint id PK
    bigint word_puzzle_id FK
    bigint dictionary_id FK
    varchar answer_text "역정규화, 공백 제거된 순수 정답 문자열"
    varchar direction_code "ACROSS / DOWN"
    int start_row
    int start_col
    int clue_number
    varchar clue_type_code "DEFINITION / VERSE"
    text clue_text
    timestamptz created_at
  }



  WORD_PUZZLE_ATTEMPT {
    bigint id PK
    bigint member_id FK
    bigint word_puzzle_id FK
    varchar attempt_status_code "IN_PROGRESS / COMPLETED"
    int score "nullable, COMPLETED 시점에 일괄 산정"
    int wrong_submission_count
    int hint_usage_count
    int elapsed_seconds "누적 경과 시간"
    timestamptz started_at
    timestamptz completed_at
    timestamptz created_at
    timestamptz updated_at
  }



  WORD_PUZZLE_ATTEMPT_CELL {
    bigint id PK
    bigint word_puzzle_attempt_id FK
    int row_index "UK (attempt_id, row, col)"
    int col_index "UK (attempt_id, row, col)"
    varchar input_letter "nullable, 사용자가 입력한 글자"
    boolean is_revealed "힌트로 공개된 칸 여부"
    timestamptz created_at
    timestamptz updated_at
  }



  WORD_PUZZLE_HINT_USAGE {
    bigint id PK
    bigint word_puzzle_attempt_id FK
    bigint word_puzzle_entry_id FK
    int row_index "nullable, 글자 공개 시 사용"
    int col_index "nullable, 글자 공개 시 사용"
    varchar hint_type_code "REVEAL_LETTER / CHECK_WORD"
    timestamptz created_at
  }



  MEMBER_DICTIONARY_PROGRESS {
    bigint id PK
    bigint member_id FK "UK (member_id, dictionary_id)"
    bigint dictionary_id FK "UK (member_id, dictionary_id)"
    int total_solved_count
    int word_level "1~4, total_solved_count 기반 산정"
    timestamptz last_solved_at
    timestamptz created_at
    timestamptz updated_at
  }

```

## 제약 조건

- `WORD_PUZZLE`
    - `puzzle_status_code`는 `DRAFT`, `PUBLISHED`, `ARCHIVED` 중 하나
- `WORD_PUZZLE_ENTRY`
    - `(word_puzzle_id, clue_number, direction_code)` 유니크
    - `direction_code`는 `ACROSS`, `DOWN` 중 하나
    - `clue_type_code`는 `DEFINITION`, `VERSE` 중 하나
- `WORD_PUZZLE_ATTEMPT`
    - `(member_id, word_puzzle_id, attempt_status_code = 'IN_PROGRESS')` 조합으로 진행 중인 attempt는 최대 1개
- `WORD_PUZZLE_ATTEMPT_CELL`
    - `(word_puzzle_attempt_id, row_index, col_index)` 유니크
- `WORD_PUZZLE_HINT_USAGE`
    - `hint_type_code`는 `REVEAL_LETTER`, `CHECK_WORD` 중 하나
    - `REVEAL_LETTER` 시 `row_index`, `col_index` 필수. `CHECK_WORD` 시 null
- `MEMBER_DICTIONARY_PROGRESS`
    - `(member_id, dictionary_id)` 유니크

## 퍼즐 상태 라이프사이클

```
DRAFT → PUBLISHED ↔ ARCHIVED
```

| 상태 | 설명 |
|---|---|
| `DRAFT` | 관리자가 퍼즐을 생성/편집 중. 플레이어에게 비공개 |
| `PUBLISHED` | 게시 완료. 퍼즐 목록에 노출되어 플레이 가능 |
| `ARCHIVED` | 비공개 처리. 기존 진행 중인 attempt는 유지(이어하기 가능)되나 신규 시작 불가 (신규 attempt 생성 시 서버는 `PUZZLE_NOT_AVAILABLE` 에러 반환) |

## 단어 학습 레벨 (`word_level`) 규칙

| 레벨 | 조건 (`total_solved_count`) | 설명 |
|---|---|---|
| 1 | 0 ~ 2회 | 처음 만남 |
| 2 | 3 ~ 5회 | 익숙해지는 중 |
| 3 | 6 ~ 9회 | 잘 알고 있음 |
| 4 | 10회 이상 | 완전히 습득 |

## 점수 산정 공식

| 항목 | 산정 규칙 |
|---|---|
| 기본 점수 | EASY: 500, NORMAL: 1000, HARD: 1500 |
| 힌트 감점 | 1회당 -50점 |
| 오답 제출 감점 | 1회당 -100점 |
| 시간 보너스 | 기준 시간 이내 완료 시 최대 +500점 (난이도별 기준 시간: EASY 5분, NORMAL 10분, HARD 20분). 기준 시간 초과 시 0점. 기준 시간 이내일 경우 `500 * (1 - elapsed / 기준시간)` |
| 최저 점수 | 0점 (음수 방지) |
