# 성경 타자 ERD

```mermaid
erDiagram
    MEMBER {
        bigint id PK
        uuid uid UK
        citext email UK
        varchar nickname
        enum member_role
        varchar profile_image_url
        timestamptz created_at
        timestamptz updated_at
    }

    BIBLE_TYPING_SESSION {
        bigint id PK
        uuid session_key UK
        bigint member_id FK
        bigint translation_id
        int book_order
        int chapter_number
        int total_verses
        int completed_verses
        int total_typed_chars
        int total_correct_chars
        double accuracy
        double cpm
        int total_elapsed_seconds
        timestamptz started_at
        timestamptz ended_at
        timestamptz created_at
        timestamptz updated_at
    }

    BIBLE_TYPING_VERSE {
        bigint session_id PK, FK
        int verse_number PK
        text original_text
        text typed_text
        int elapsed_seconds
        boolean completed
        double accuracy
        double cpm
        timestamptz created_at
        timestamptz updated_at
    }

    MEMBER ||--o{ BIBLE_TYPING_SESSION: owns
    BIBLE_TYPING_SESSION ||--o{ BIBLE_TYPING_VERSE: contains
```

## 제약 조건

- `BIBLE_TYPING_SESSION`
  - `session_key` 유니크.
  - `(member_id, translation_id, book_order, chapter_number)` 유니크.
- `BIBLE_TYPING_VERSE`
  - `(session_id, verse_number)` 복합 PK.
