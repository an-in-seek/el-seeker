# 성경 가로세로 낱말 퍼즐 REST API Spec

> 모든 API는 인증 필요 (JWT Access Token in HttpOnly Cookie)
>
> Base URL: `/api/v1/game/word-puzzles`

---

## 1. 퍼즐 목록 조회

게시된 퍼즐 목록과 현재 사용자의 이어하기 가능 여부를 반환한다.

```
GET /api/v1/game/word-puzzles?theme={themeCode}&difficulty={difficultyCode}&page={page}&size={size}
```

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| theme | string | N | 테마 코드 필터. 미지정 시 전체 |
| difficulty | string | N | 난이도 코드 필터. 미지정 시 전체 |
| page | int | N | 페이지 번호 (0부터 시작, 기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 20) |

### Response `200 OK`

```json
{
  "content": [
    {
      "puzzleId": 1,
      "title": "창세기 인물 퍼즐",
      "themeCode": "GENESIS",
      "difficultyCode": "EASY",
      "boardWidth": 10,
      "boardHeight": 10,
      "publishedAt": "2026-03-01T00:00:00",
      "inProgressAttemptId": 42
    },
    {
      "puzzleId": 2,
      "title": "십계명 핵심 단어",
      "themeCode": "TEN_COMMANDMENTS",
      "difficultyCode": "NORMAL",
      "boardWidth": 12,
      "boardHeight": 12,
      "publishedAt": "2026-03-01T00:00:00",
      "inProgressAttemptId": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| content | array | 퍼즐 목록 |
| content[].puzzleId | long | 퍼즐 ID |
| content[].title | string | 퍼즐 제목 |
| content[].themeCode | string | 테마 코드 |
| content[].difficultyCode | string | 난이도 코드 |
| content[].boardWidth | int | 보드 가로 크기 |
| content[].boardHeight | int | 보드 세로 크기 |
| content[].publishedAt | datetime | 게시일 |
| content[].inProgressAttemptId | long \| null | 진행 중인 attempt ID. null이면 신규, 값이 있으면 이어하기 가능 |
| page | int | 현재 페이지 번호 |
| size | int | 페이지 크기 |
| totalElements | long | 전체 퍼즐 수 |
| totalPages | int | 전체 페이지 수 |

---

## 2. 퍼즐 시작 (신규)

새로운 attempt를 생성하고 보드 구조 + 단서 목록을 반환한다.

```
POST /api/v1/game/word-puzzles/{puzzleId}/attempts
```

### Path Parameters

| 파라미터 | 타입 | 설명 |
|---|---|---|
| puzzleId | long | 퍼즐 ID |

### Response `201 Created`

```json
{
  "attemptId": 42,
  "elapsedSeconds": 0,
  "board": {
    "width": 10,
    "height": 10
  },
  "entries": [
    {
      "entryId": 1,
      "clueNumber": 1,
      "directionCode": "ACROSS",
      "startRow": 0,
      "startCol": 2,
      "length": 3,
      "clueTypeCode": "DEFINITION",
      "clueText": "하나님이 세상을 만드신 행위"
    },
    {
      "entryId": 2,
      "clueNumber": 1,
      "directionCode": "DOWN",
      "startRow": 0,
      "startCol": 2,
      "length": 4,
      "clueTypeCode": "VERSE",
      "clueText": "태초에 하나님이 천지를 ____하시니라"
    }
  ],
  "cells": [
    { "row": 0, "col": 2, "inputLetter": null, "isRevealed": false },
    { "row": 0, "col": 3, "inputLetter": null, "isRevealed": false }
  ]
}
```

### Error `400 Bad Request`

퍼즐이 PUBLISHED 상태가 아닌 경우 (ARCHIVED 등).

```json
{
  "error": "PUZZLE_NOT_AVAILABLE",
  "message": "현재 플레이할 수 없는 퍼즐입니다."
}
```

### Error `409 Conflict`

이미 진행 중인 attempt가 존재하는 경우.

```json
{
  "error": "IN_PROGRESS_ATTEMPT_EXISTS",
  "message": "이미 진행 중인 퍼즐이 있습니다.",
  "inProgressAttemptId": 42
}
```

---

## 3. 퍼즐 이어하기

진행 중인 attempt의 저장된 상태를 복원하여 반환한다.

```
GET /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}
```

### Path Parameters

| 파라미터 | 타입 | 설명 |
|---|---|---|
| puzzleId | long | 퍼즐 ID |
| attemptId | long | attempt ID |

### Response `200 OK`

```json
{
  "attemptId": 42,
  "elapsedSeconds": 185,
  "board": {
    "width": 10,
    "height": 10
  },
  "entries": [
    {
      "entryId": 1,
      "clueNumber": 1,
      "directionCode": "ACROSS",
      "startRow": 0,
      "startCol": 2,
      "length": 3,
      "clueTypeCode": "DEFINITION",
      "clueText": "하나님이 세상을 만드신 행위"
    }
  ],
  "cells": [
    { "row": 0, "col": 2, "inputLetter": "창", "isRevealed": false },
    { "row": 0, "col": 3, "inputLetter": "조", "isRevealed": true },
    { "row": 0, "col": 4, "inputLetter": null, "isRevealed": false }
  ]
}
```

### Error `404 Not Found`

attempt가 존재하지 않거나 해당 사용자의 것이 아닌 경우.

---

## 4. 셀 저장 (자동 저장)

글자 입력/삭제 시 변경된 셀들을 일괄 저장한다.

```
PATCH /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/cells
```

### Path Parameters

| 파라미터 | 타입 | 설명 |
|---|---|---|
| puzzleId | long | 퍼즐 ID |
| attemptId | long | attempt ID |

### Request Body

```json
{
  "cells": [
    { "row": 0, "col": 2, "letter": "창" },
    { "row": 0, "col": 3, "letter": null }
  ],
  "elapsedSeconds": 45
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| cells | array | Y | 변경된 셀 목록 |
| cells[].row | int | Y | 행 인덱스 |
| cells[].col | int | Y | 열 인덱스 |
| cells[].letter | string \| null | Y | 입력된 글자. null이면 삭제 |
| elapsedSeconds | int | Y | 클라이언트 측 누적 경과 시간 (초). 서버는 `MAX(저장값, 수신값)`으로 처리 |

### Response `200 OK`

```json
{}
```

### Error `400 Bad Request`

attempt가 COMPLETED 상태인 경우.

```json
{
  "error": "ATTEMPT_ALREADY_COMPLETED",
  "message": "이미 완료된 퍼즐입니다."
}
```

---

## 5. 힌트 — 글자 공개

선택한 칸의 정답 글자를 공개한다.

```
POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/hints/reveal-letter
```

### Path Parameters

| 파라미터 | 타입 | 설명 |
|---|---|---|
| puzzleId | long | 퍼즐 ID |
| attemptId | long | attempt ID |

### Request Body

```json
{
  "entryId": 1,
  "row": 0,
  "col": 2,
  "elapsedSeconds": 60
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| entryId | long | Y | 해당 단어의 entry ID |
| row | int | Y | 행 인덱스 |
| col | int | Y | 열 인덱스 |
| elapsedSeconds | int | Y | 누적 경과 시간 (초). 서버는 `MAX(저장값, 수신값)`으로 처리 |

### Response `200 OK`

```json
{
  "row": 0,
  "col": 2,
  "letter": "창",
  "hintUsageCount": 3
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| row | int | 공개된 셀 행 |
| col | int | 공개된 셀 열 |
| letter | string | 공개된 정답 글자 |
| hintUsageCount | int | 누적 힌트 사용 횟수 |

### Error `400 Bad Request`

이미 공개된 칸이거나 attempt가 COMPLETED 상태인 경우.

---

## 6. 힌트 — 단어 확인

현재 단어의 입력된 글자가 맞는지 셀별로 확인한다. 빈 셀(`null`)은 오답(`correct: false`)으로 처리한다.

```
POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/hints/check-word
```

### Path Parameters

| 파라미터 | 타입 | 설명 |
|---|---|---|
| puzzleId | long | 퍼즐 ID |
| attemptId | long | attempt ID |

### Request Body

```json
{
  "entryId": 1,
  "elapsedSeconds": 90
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| entryId | long | Y | 확인할 단어의 entry ID |
| elapsedSeconds | int | Y | 누적 경과 시간 (초). 서버는 `MAX(저장값, 수신값)`으로 처리 |

### Response `200 OK`

```json
{
  "results": [
    { "row": 0, "col": 2, "correct": true },
    { "row": 0, "col": 3, "correct": true },
    { "row": 0, "col": 4, "correct": false }
  ],
  "hintUsageCount": 4
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| results | array | 셀별 정답 여부 |
| results[].row | int | 행 인덱스 |
| results[].col | int | 열 인덱스 |
| results[].correct | boolean | 해당 셀의 정답 여부. 빈 셀은 `false` |
| hintUsageCount | int | 누적 힌트 사용 횟수 |

### Error `400 Bad Request`

attempt가 COMPLETED 상태인 경우.

---

## 7. 전체 제출

모든 셀의 정답을 검증하고 결과를 반환한다.

```
POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/submit
```

### Path Parameters

| 파라미터 | 타입 | 설명 |
|---|---|---|
| puzzleId | long | 퍼즐 ID |
| attemptId | long | attempt ID |

### Request Body

```json
{
  "elapsedSeconds": 300
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| elapsedSeconds | int | Y | 누적 경과 시간 (초). 서버는 `MAX(저장값, 수신값)`으로 처리 |

### Response `200 OK` — 정답

```json
{
  "result": "CORRECT",
  "score": 850,
  "elapsedSeconds": 300,
  "hintUsageCount": 2,
  "wrongSubmissionCount": 1,
  "words": [
    {
      "surfaceForm": "창조",
      "dictionaryDefinition": "하나님이 세상을 만드신 행위",
      "originalLanguageCode": "HEBREW",
      "originalLexeme": "בָּרָא",
      "references": [
        {
          "verseReference": "창세기 1:1",
          "verseExcerpt": "태초에 하나님이 천지를 창조하시니라"
        }
      ]
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| result | string | `CORRECT` |
| score | int | 최종 점수 |
| elapsedSeconds | int | 총 소요 시간 (초) |
| hintUsageCount | int | 힌트 사용 횟수 |
| wrongSubmissionCount | int | 오답 제출 횟수 |
| words | array | 풀었던 단어들의 학습 정보 |
| words[].surfaceForm | string | 단어 원형 |
| words[].dictionaryDefinition | string | 사전적 정의 |
| words[].originalLanguageCode | string \| null | 원어 코드 (HEBREW / GREEK). 원어 정보가 없는 사전 항목은 null |
| words[].originalLexeme | string \| null | 원어 어휘. 원어 정보가 없는 사전 항목은 null |
| words[].references | array | 성경 구절 참조 목록 |

### Response `200 OK` — 오답

```json
{
  "result": "WRONG",
  "wrongCells": [
    { "row": 2, "col": 5 },
    { "row": 3, "col": 5 }
  ],
  "wrongSubmissionCount": 2,
  "elapsedSeconds": 300
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| result | string | `WRONG` |
| wrongCells | array | 틀린 셀 좌표 목록 |
| wrongCells[].row | int | 행 인덱스 |
| wrongCells[].col | int | 열 인덱스 |
| wrongSubmissionCount | int | 누적 오답 제출 횟수 |
| elapsedSeconds | int | 서버에 저장된 누적 경과 시간 (초) |

### Error `400 Bad Request`

빈 칸이 존재하거나 attempt가 COMPLETED 상태인 경우.

```json
{
  "error": "EMPTY_CELLS_EXIST",
  "message": "아직 채워지지 않은 칸이 있습니다."
}
```

---

## 공통 에러 응답

| Status Code | 상황 |
|---|---|
| `401 Unauthorized` | 인증 실패 (JWT 토큰 없음/만료) |
| `403 Forbidden` | 다른 사용자의 attempt에 접근 |
| `404 Not Found` | 퍼즐 또는 attempt가 존재하지 않음 |
| `400 Bad Request` | 잘못된 요청 (완료된 퍼즐, 빈 칸 존재, ARCHIVED 퍼즐 신규 시작 등) |
| `409 Conflict` | 진행 중인 attempt가 이미 존재 |

```json
{
  "error": "ERROR_CODE",
  "message": "사용자에게 표시할 메시지"
}
```

---

# 관리자 API

> 모든 관리자 API는 `ADMIN` 역할 인증 필요 (JWT Access Token in HttpOnly Cookie)
>
> Base URL: `/api/v1/admin/word-puzzles`

---

## A-1. 퍼즐 목록 조회 (관리자)

전체 퍼즐 목록을 상태 구분 없이 반환한다.

```
GET /api/v1/admin/word-puzzles?page={page}&size={size}
```

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| page | int | N | 페이지 번호 (0부터 시작, 기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 20) |

### Response `200 OK`

```json
{
  "content": [
    {
      "id": 1,
      "title": "창세기 인물 퍼즐",
      "themeCode": "GENESIS",
      "difficultyCode": "EASY",
      "boardWidth": 10,
      "boardHeight": 10,
      "puzzleStatusCode": "DRAFT",
      "publishedAt": null,
      "createdAt": "2026-03-01T00:00:00Z",
      "updatedAt": "2026-03-01T00:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

---

## A-2. 퍼즐 단건 조회 (관리자)

```
GET /api/v1/admin/word-puzzles/{id}
```

### Response `200 OK`

```json
{
  "id": 1,
  "title": "창세기 인물 퍼즐",
  "themeCode": "GENESIS",
  "difficultyCode": "EASY",
  "boardWidth": 10,
  "boardHeight": 10,
  "puzzleStatusCode": "DRAFT",
  "publishedAt": null,
  "createdAt": "2026-03-01T00:00:00Z",
  "updatedAt": "2026-03-01T00:00:00Z"
}
```

### Error `404 Not Found`

퍼즐이 존재하지 않는 경우.

---

## A-3. 퍼즐 등록

```
POST /api/v1/admin/word-puzzles
```

### Request Body

```json
{
  "title": "창세기 인물 퍼즐",
  "themeCode": "GENESIS",
  "difficultyCode": "EASY",
  "boardWidth": 10,
  "boardHeight": 10
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| title | string | Y | 퍼즐 제목 (최대 200자) |
| themeCode | string | Y | 테마 코드 |
| difficultyCode | string | Y | 난이도 코드 (EASY / NORMAL / HARD) |
| boardWidth | int | Y | 보드 가로 크기 |
| boardHeight | int | Y | 보드 세로 크기 |

### Response `201 Created`

생성된 퍼즐 정보 (A-2 응답과 동일 구조). 초기 상태는 `DRAFT`.

---

## A-4. 퍼즐 수정

```
PUT /api/v1/admin/word-puzzles/{id}
```

### Request Body

A-3과 동일 구조.

### Response `200 OK`

수정된 퍼즐 정보.

### Error `404 Not Found`

퍼즐이 존재하지 않는 경우.

---

## A-5. 퍼즐 상태 변경

퍼즐 상태를 전환한다. `DRAFT → PUBLISHED`, `PUBLISHED ↔ ARCHIVED` 전환만 허용.

```
PATCH /api/v1/admin/word-puzzles/{id}/status
```

### Request Body

```json
{
  "status": "PUBLISHED"
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| status | string | Y | 변경할 상태 (PUBLISHED / ARCHIVED) |

### Response `200 OK`

변경된 퍼즐 정보.

### Error `400 Bad Request`

허용되지 않는 상태 전환인 경우.

```json
{
  "error": "INVALID_STATUS_TRANSITION",
  "message": "DRAFT 상태에서 ARCHIVED로 전환할 수 없습니다."
}
```

### 상태 전환 규칙

| 현재 상태 | 허용 전환 |
|---|---|
| DRAFT | PUBLISHED |
| PUBLISHED | ARCHIVED |
| ARCHIVED | PUBLISHED |

---

## A-6. 퍼즐 삭제

DRAFT 상태의 퍼즐만 삭제 가능. 연관된 entries도 함께 삭제된다.

```
DELETE /api/v1/admin/word-puzzles/{id}
```

### Response `204 No Content`

### Error `400 Bad Request`

DRAFT가 아닌 퍼즐을 삭제하려는 경우.

```json
{
  "error": "CANNOT_DELETE_PUBLISHED_PUZZLE",
  "message": "게시된 퍼즐은 삭제할 수 없습니다. 비공개(ARCHIVED) 처리하세요."
}
```

---

## A-7. 퍼즐 단서(Entry) 목록 조회

특정 퍼즐에 속하는 단서 목록을 반환한다.

```
GET /api/v1/admin/word-puzzles/{puzzleId}/entries?page={page}&size={size}
```

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| page | int | N | 페이지 번호 (기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 50) |

### Response `200 OK`

```json
{
  "content": [
    {
      "id": 1,
      "clueNumber": 1,
      "directionCode": "ACROSS",
      "dictionaryId": 42,
      "dictionaryTerm": "창조",
      "answerText": "창조",
      "startRow": 0,
      "startCol": 2,
      "length": 2,
      "clueTypeCode": "DEFINITION",
      "clueText": "하나님이 세상을 만드신 행위",
      "createdAt": "2026-03-01T00:00:00Z"
    }
  ],
  "page": 0,
  "size": 50,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

---

## A-8. 퍼즐 단서 단건 조회

```
GET /api/v1/admin/word-puzzles/{puzzleId}/entries/{entryId}
```

### Response `200 OK`

```json
{
  "id": 1,
  "clueNumber": 1,
  "directionCode": "ACROSS",
  "dictionaryId": 42,
  "dictionaryTerm": "창조",
  "dictionaryDescription": "하나님이 세상을 만드신 행위",
  "answerText": "창조",
  "startRow": 0,
  "startCol": 2,
  "length": 2,
  "clueTypeCode": "DEFINITION",
  "clueText": "하나님이 세상을 만드신 행위",
  "createdAt": "2026-03-01T00:00:00Z"
}
```

---

## A-9. 퍼즐 단서 등록

```
POST /api/v1/admin/word-puzzles/{puzzleId}/entries
```

### Request Body

```json
{
  "dictionaryId": 42,
  "answerText": "창조",
  "directionCode": "ACROSS",
  "startRow": 0,
  "startCol": 2,
  "clueNumber": 1,
  "clueTypeCode": "DEFINITION",
  "clueText": "하나님이 세상을 만드신 행위"
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| dictionaryId | long | Y | 연결할 사전 단어 ID |
| answerText | string | Y | 정답 문자열 (공백 제거된 순수 텍스트) |
| directionCode | string | Y | 방향 (ACROSS / DOWN) |
| startRow | int | Y | 시작 행 (0-based) |
| startCol | int | Y | 시작 열 (0-based) |
| clueNumber | int | Y | 단서 번호 |
| clueTypeCode | string | Y | 단서 유형 (DEFINITION / VERSE) |
| clueText | string | Y | 플레이어에게 보여줄 힌트 텍스트 |

### Response `201 Created`

생성된 단서 정보 (A-8 응답과 동일 구조).

### Error `409 Conflict`

동일 퍼즐 내 `(clue_number, direction_code)` 조합이 이미 존재하는 경우.

---

## A-10. 퍼즐 단서 수정

```
PUT /api/v1/admin/word-puzzles/{puzzleId}/entries/{entryId}
```

### Request Body

A-9와 동일 구조.

### Response `200 OK`

수정된 단서 정보.

---

## A-11. 퍼즐 단서 삭제

```
DELETE /api/v1/admin/word-puzzles/{puzzleId}/entries/{entryId}
```

### Response `204 No Content`

---

## A-12. 사전(Dictionary) 검색 (단서 등록용)

단서 등록/수정 시 연결할 사전 단어를 검색한다.

```
GET /api/v1/admin/word-puzzles/dictionaries?term={keyword}&page={page}&size={size}
```

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| term | string | Y | 검색 키워드 (부분 일치) |
| page | int | N | 페이지 번호 (기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 20) |

### Response `200 OK`

```json
{
  "content": [
    {
      "id": 42,
      "term": "창조",
      "description": "하나님이 세상을 만드신 행위",
      "originalLanguageCode": "HEBREW",
      "originalLexeme": "בָּרָא"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 관리자 API 공통 에러 응답

| Status Code | 상황 |
|---|---|
| `401 Unauthorized` | 인증 실패 |
| `403 Forbidden` | ADMIN 역할이 아닌 사용자 |
| `404 Not Found` | 퍼즐 또는 단서가 존재하지 않음 |
| `400 Bad Request` | 잘못된 요청 (유효하지 않은 상태 전환, 삭제 불가 등) |
| `409 Conflict` | 중복 데이터 (단서 번호+방향 유니크 제약 위반) |

---

## `elapsedSeconds` 처리 규칙

클라이언트와 서버 간 경과 시간 동기화를 위해 다음 규칙을 적용한다:

- 모든 mutation 요청(셀 저장, 힌트, 제출)에 `elapsedSeconds`를 필수로 전송
- 서버는 `MAX(현재 저장된 값, 수신된 값)`을 적용하여 시간이 역행하지 않도록 보장
- 클라이언트는 페이지 진입 시 서버로부터 받은 `elapsedSeconds`를 기준으로 타이머를 시작
