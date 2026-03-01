# 성경 가로세로 낱말 퍼즐 시퀀스 다이어그램

## 1. 퍼즐 목록 조회

```mermaid
sequenceDiagram
    actor Player as 플레이어
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Player->>Client: 퍼즐 목록 페이지 접속
    Client->>Server: GET /api/v1/game/word-puzzles?theme=&difficulty=
    Server->>DB: SELECT WORD_PUZZLE (status = PUBLISHED)
    DB-->>Server: 퍼즐 목록
    Server->>DB: SELECT WORD_PUZZLE_ATTEMPT (member_id, status = IN_PROGRESS)
    DB-->>Server: 진행 중인 attempt 목록
    Server-->>Client: 퍼즐 목록 + 이어하기 가능 여부
    Client-->>Player: 퍼즐 목록 표시 (신규 / 이어하기 뱃지)
```

## 2. 퍼즐 시작 (신규)

```mermaid
sequenceDiagram
    actor Player as 플레이어
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Player->>Client: 퍼즐 선택 (신규)
    Client->>Server: POST /api/v1/game/word-puzzles/{puzzleId}/attempts

    Server->>DB: SELECT WORD_PUZZLE (puzzle_status_code 조회)
    DB-->>Server: 퍼즐 상태

    alt 퍼즐이 PUBLISHED 상태가 아님 (ARCHIVED 등)
        Server-->>Client: 400 Bad Request (PUZZLE_NOT_AVAILABLE)
        Client-->>Player: "현재 플레이할 수 없는 퍼즐입니다" 안내
    else 퍼즐이 PUBLISHED 상태
        Server->>DB: SELECT WORD_PUZZLE_ATTEMPT (member_id, puzzle_id, status = IN_PROGRESS)
        DB-->>Server: 진행 중 attempt 조회 결과

        alt 진행 중인 attempt 존재
            Server-->>Client: 409 Conflict (IN_PROGRESS_ATTEMPT_EXISTS)
            Client-->>Player: "이미 진행 중인 퍼즐이 있습니다" 안내
        else 진행 중인 attempt 없음
            rect rgb(240, 248, 255)
                Note over Server,DB: @Transactional — ATTEMPT + CELL 생성을 하나의 트랜잭션으로 처리
                Server->>DB: INSERT WORD_PUZZLE_ATTEMPT (status = IN_PROGRESS, started_at = now)
                DB-->>Server: attempt 생성 완료
                Server->>DB: INSERT WORD_PUZZLE_ATTEMPT_CELL (보드의 모든 입력 가능 칸)
                DB-->>Server: cell 초기화 완료
            end

            Server->>DB: SELECT WORD_PUZZLE_ENTRY + DICTIONARY (단서 목록)
            DB-->>Server: 보드 배치 + 단서 데이터
            Server-->>Client: attemptId, 보드 구조, 단서 목록 (가로/세로)
            Client-->>Player: 빈 보드 + 단서 목록 표시, 타이머 시작
        end
    end
```

## 3. 퍼즐 이어하기

```mermaid
sequenceDiagram
    actor Player as 플레이어
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Player->>Client: 퍼즐 선택 (이어하기)
    Client->>Server: GET /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}
    Server->>DB: SELECT WORD_PUZZLE_ATTEMPT (elapsed_seconds)
    DB-->>Server: attempt 데이터
    Server->>DB: SELECT WORD_PUZZLE_ATTEMPT_CELL (입력된 글자, 공개 여부)
    DB-->>Server: 저장된 셀 상태
    Server->>DB: SELECT WORD_PUZZLE_ENTRY + DICTIONARY (단서 목록)
    DB-->>Server: 보드 배치 + 단서 데이터
    Server-->>Client: 보드 구조, 단서 목록, 저장된 셀 상태, 경과 시간
    Client-->>Player: 복원된 보드 표시, 타이머 이어서 시작
```

## 4. 글자 입력 및 자동 저장

```mermaid
sequenceDiagram
    actor Player as 플레이어
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Player->>Client: 단서 클릭 또는 칸 클릭
    Client-->>Player: 해당 칸 포커스 + 단어 하이라이트

    Player->>Client: 글자 입력
    Client-->>Player: 글자 표시, 다음 칸 자동 이동

    Note over Client: 디바운싱 — 연속 입력 시 마지막 입력 후<br/>일정 시간(예: 500ms) 대기 후 일괄 전송
    Client->>Server: PATCH /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/cells
    Note right of Client: { cells: [{row, col, letter}], elapsedSeconds }

    alt attempt가 COMPLETED 상태
        Server-->>Client: 400 Bad Request (ATTEMPT_ALREADY_COMPLETED)
    else attempt가 IN_PROGRESS 상태
        Server->>Server: 셀 좌표 유효성 검증 (보드 범위, attempt 소유자)
        Server->>DB: UPDATE WORD_PUZZLE_ATTEMPT_CELL (input_letter)
        Server->>DB: UPDATE WORD_PUZZLE_ATTEMPT (elapsed_seconds = MAX(저장값, 수신값))
        DB-->>Server: 저장 완료
        Server-->>Client: 200 OK
    end
```

## 5. 글자 삭제 및 자동 저장

```mermaid
sequenceDiagram
    actor Player as 플레이어
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Player->>Client: Backspace 입력
    Client-->>Player: 현재 칸 글자 삭제

    Note over Client: 디바운싱 — 연속 삭제 시 마지막 입력 후<br/>일정 시간(예: 500ms) 대기 후 일괄 전송
    Client->>Server: PATCH /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/cells
    Note right of Client: { cells: [{row, col, letter: null}], elapsedSeconds }

    alt attempt가 COMPLETED 상태
        Server-->>Client: 400 Bad Request (ATTEMPT_ALREADY_COMPLETED)
    else attempt가 IN_PROGRESS 상태
        Server->>DB: UPDATE WORD_PUZZLE_ATTEMPT_CELL (input_letter = null)
        Server->>DB: UPDATE WORD_PUZZLE_ATTEMPT (elapsed_seconds = MAX(저장값, 수신값))
        DB-->>Server: 저장 완료
        Server-->>Client: 200 OK
    end
```

## 6. 힌트 — 글자 공개

```mermaid
sequenceDiagram
    actor Player as 플레이어
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Player->>Client: 글자 공개 힌트 요청
    Client->>Server: POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/hints/reveal-letter
    Note right of Client: { entryId, row, col, elapsedSeconds }

    alt attempt가 COMPLETED 상태 또는 이미 공개된 칸
        Server-->>Client: 400 Bad Request
    else 정상 처리
        Server->>DB: SELECT WORD_PUZZLE_ENTRY.answer_text (정답 글자 조회)
        DB-->>Server: 정답 글자
        Server->>DB: INSERT WORD_PUZZLE_HINT_USAGE (REVEAL_LETTER, row, col)
        Server->>DB: UPDATE WORD_PUZZLE_ATTEMPT_CELL (input_letter, is_revealed = true)
        Server->>DB: UPDATE WORD_PUZZLE_ATTEMPT (hint_usage_count + 1, elapsed_seconds = MAX(저장값, 수신값))
        DB-->>Server: 저장 완료
        Server-->>Client: 공개된 글자, hintUsageCount
        Client-->>Player: 해당 칸에 정답 글자 표시 (공개 스타일)
    end
```

## 7. 힌트 — 단어 확인

```mermaid
sequenceDiagram
    actor Player as 플레이어
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Player->>Client: 단어 확인 힌트 요청
    Client->>Server: POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/hints/check-word
    Note right of Client: { entryId, elapsedSeconds }

    alt attempt가 COMPLETED 상태
        Server-->>Client: 400 Bad Request (ATTEMPT_ALREADY_COMPLETED)
    else 정상 처리
        Server->>DB: SELECT WORD_PUZZLE_ENTRY.answer_text (정답 단어 조회)
        DB-->>Server: 정답 단어
        Server->>DB: SELECT WORD_PUZZLE_ATTEMPT_CELL (해당 단어 셀들의 입력값)
        DB-->>Server: 입력된 글자들
        Server->>Server: 셀별 비교 (빈 셀은 오답 처리)
        Server->>DB: INSERT WORD_PUZZLE_HINT_USAGE (CHECK_WORD, row/col = null)
        Server->>DB: UPDATE WORD_PUZZLE_ATTEMPT (hint_usage_count + 1, elapsed_seconds = MAX(저장값, 수신값))
        DB-->>Server: 저장 완료
        Server-->>Client: 셀별 정답 여부 [{row, col, correct: true/false}], hintUsageCount
        Client-->>Player: 맞은 칸 초록, 틀린 칸 빨강 표시
    end
```

## 8. 전체 제출 — 정답

```mermaid
sequenceDiagram
    actor Player as 플레이어
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Player->>Client: 전체 제출 버튼 클릭
    Client->>Server: POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/submit
    Note right of Client: { elapsedSeconds }

    alt attempt가 COMPLETED 상태
        Server-->>Client: 400 Bad Request (ATTEMPT_ALREADY_COMPLETED)
    else 빈 셀이 존재
        Server-->>Client: 400 Bad Request (EMPTY_CELLS_EXIST)
    else 정상 처리
        Server->>DB: SELECT WORD_PUZZLE_ATTEMPT_CELL (전체 입력값)
        DB-->>Server: 입력된 글자들
        Server->>DB: SELECT WORD_PUZZLE_ENTRY.answer_text (정답 데이터)
        DB-->>Server: 정답 데이터
        Server->>Server: 전체 셀 정답 비교

        alt 모두 정답
            rect rgb(240, 248, 255)
                Note over Server,DB: @Transactional — ATTEMPT 완료 + PROGRESS 갱신을 하나의 트랜잭션으로 처리
                Server->>Server: 점수 산정 (기본 점수 - 힌트 감점 - 오답 감점 + 시간 보너스)
                Server->>DB: UPDATE WORD_PUZZLE_ATTEMPT (status = COMPLETED, score, elapsed_seconds, completed_at)
                Server->>DB: UPSERT MEMBER_DICTIONARY_PROGRESS (total_solved_count + 1, word_level 갱신)
                DB-->>Server: 저장 완료
            end
            Server->>DB: SELECT DICTIONARY + DICTIONARY_REFERENCE (학습 정보)
            DB-->>Server: 단어 학습 데이터
            Server-->>Client: {result: CORRECT, score, elapsedSeconds, hintCount, words: [학습 정보]}
            Client-->>Player: 결과 화면 (점수, 소요 시간, 힌트 횟수, 단어 학습 정보)
        end
    end
```

## 9. 전체 제출 — 오답

```mermaid
sequenceDiagram
    actor Player as 플레이어
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Player->>Client: 전체 제출 버튼 클릭
    Client->>Server: POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/submit
    Note right of Client: { elapsedSeconds }

    alt attempt가 COMPLETED 상태
        Server-->>Client: 400 Bad Request (ATTEMPT_ALREADY_COMPLETED)
    else 빈 셀이 존재
        Server-->>Client: 400 Bad Request (EMPTY_CELLS_EXIST)
    else 정상 처리
        Server->>DB: SELECT WORD_PUZZLE_ATTEMPT_CELL (전체 입력값)
        DB-->>Server: 입력된 글자들
        Server->>DB: SELECT WORD_PUZZLE_ENTRY.answer_text (정답 데이터)
        DB-->>Server: 정답 데이터
        Server->>Server: 전체 셀 정답 비교

        alt 오답 존재
            Server->>DB: UPDATE WORD_PUZZLE_ATTEMPT (wrong_submission_count + 1, elapsed_seconds = MAX(저장값, 수신값))
            DB-->>Server: 저장 완료
            Server-->>Client: {result: WRONG, wrongCells: [{row, col}], wrongSubmissionCount, elapsedSeconds}
            Client-->>Player: 틀린 칸 빨간색 깜빡임, 계속 풀기
        end
    end
```

---

# 관리자 시퀀스

## A-1. 퍼즐 CRUD (등록 / 수정 / 삭제)

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Admin->>Client: 퍼즐 목록 페이지 접속
    Client->>Server: GET /api/v1/admin/word-puzzles?page=0&size=20
    Server->>DB: SELECT WORD_PUZZLE (전체 상태)
    DB-->>Server: 퍼즐 목록
    Server-->>Client: 퍼즐 목록 (DRAFT / PUBLISHED / ARCHIVED 포함)
    Client-->>Admin: 퍼즐 목록 표시

    alt 등록
        Admin->>Client: [등록] 버튼 클릭
        Client-->>Admin: 퍼즐 등록 폼 표시
        Admin->>Client: 제목, 테마, 난이도, 보드 크기 입력 후 [저장]
        Client->>Server: POST /api/v1/admin/word-puzzles
        Note right of Client: { title, themeCode, difficultyCode, boardWidth, boardHeight }
        Server->>DB: INSERT WORD_PUZZLE (status = DRAFT)
        DB-->>Server: 생성 완료
        Server-->>Client: 생성된 퍼즐 정보
        Client-->>Admin: 퍼즐 목록으로 이동
    end

    alt 수정
        Admin->>Client: [수정] 버튼 클릭
        Client->>Server: GET /api/v1/admin/word-puzzles/{id}
        Server->>DB: SELECT WORD_PUZZLE
        DB-->>Server: 퍼즐 데이터
        Server-->>Client: 퍼즐 상세 정보
        Client-->>Admin: 퍼즐 수정 폼 표시 (기존 값 채움)
        Admin->>Client: 값 수정 후 [저장]
        Client->>Server: PUT /api/v1/admin/word-puzzles/{id}
        Server->>DB: UPDATE WORD_PUZZLE
        DB-->>Server: 수정 완료
        Server-->>Client: 수정된 퍼즐 정보
        Client-->>Admin: 퍼즐 목록으로 이동
    end

    alt 삭제 (DRAFT만 가능)
        Admin->>Client: [삭제] 버튼 클릭
        Client-->>Admin: "삭제하시겠습니까?" 확인 다이얼로그
        Admin->>Client: 확인
        Client->>Server: DELETE /api/v1/admin/word-puzzles/{id}
        alt DRAFT 상태
            Server->>DB: DELETE WORD_PUZZLE + CASCADE entries
            DB-->>Server: 삭제 완료
            Server-->>Client: 204 No Content
            Client-->>Admin: 목록 갱신
        else DRAFT가 아닌 상태
            Server-->>Client: 400 Bad Request (CANNOT_DELETE_PUBLISHED_PUZZLE)
            Client-->>Admin: "게시된 퍼즐은 삭제할 수 없습니다" 안내
        end
    end
```

## A-2. 퍼즐 상태 전환 (게시 / 비공개)

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Admin->>Client: [게시] 또는 [비공개] 버튼 클릭
    Client->>Server: PATCH /api/v1/admin/word-puzzles/{id}/status
    Note right of Client: { status: "PUBLISHED" 또는 "ARCHIVED" }

    Server->>DB: SELECT WORD_PUZZLE (현재 상태 조회)
    DB-->>Server: 현재 상태

    alt 유효한 전환 (DRAFT→PUBLISHED, PUBLISHED↔ARCHIVED)
        Server->>DB: UPDATE WORD_PUZZLE (puzzle_status_code, published_at)
        Note over Server,DB: DRAFT→PUBLISHED 시 published_at = now()
        DB-->>Server: 수정 완료
        Server-->>Client: 변경된 퍼즐 정보
        Client-->>Admin: 상태 뱃지 갱신
    else 유효하지 않은 전환
        Server-->>Client: 400 Bad Request (INVALID_STATUS_TRANSITION)
        Client-->>Admin: 오류 메시지 표시
    end
```

## A-3. 퍼즐 단서(Entry) CRUD

```mermaid
sequenceDiagram
    actor Admin as 관리자
    participant Client as 브라우저
    participant Server as 서버
    participant DB as DB

    Admin->>Client: [단서 관리] 버튼 클릭
    Client->>Server: GET /api/v1/admin/word-puzzles/{puzzleId}/entries?page=0&size=50
    Server->>DB: SELECT WORD_PUZZLE_ENTRY + DICTIONARY (puzzleId 기준)
    DB-->>Server: 단서 목록
    Server-->>Client: 단서 목록 (사전 단어 포함)
    Client-->>Admin: 단서 목록 표시

    alt 단서 등록
        Admin->>Client: [단서 추가] 버튼 클릭
        Client-->>Admin: 단서 등록 폼 표시

        Admin->>Client: 사전 단어 검색
        Client->>Server: GET /api/v1/admin/word-puzzles/dictionaries?term={keyword}
        Server->>DB: SELECT DICTIONARY (term LIKE %keyword%)
        DB-->>Server: 검색 결과
        Server-->>Client: 사전 단어 목록
        Client-->>Admin: 검색 결과 드롭다운 표시
        Admin->>Client: 사전 단어 선택

        Admin->>Client: 정답, 방향, 위치, 단서 번호, 유형, 텍스트 입력 후 [저장]
        Client->>Server: POST /api/v1/admin/word-puzzles/{puzzleId}/entries
        Note right of Client: { dictionaryId, answerText, directionCode,<br/>startRow, startCol, clueNumber, clueTypeCode, clueText }

        alt 단서 번호+방향 중복
            Server-->>Client: 409 Conflict
            Client-->>Admin: "이미 존재하는 단서 번호입니다" 안내
        else 정상 처리
            Server->>DB: INSERT WORD_PUZZLE_ENTRY
            DB-->>Server: 생성 완료
            Server-->>Client: 생성된 단서 정보
            Client-->>Admin: 단서 목록으로 이동
        end
    end

    alt 단서 수정
        Admin->>Client: [수정] 버튼 클릭
        Client->>Server: GET /api/v1/admin/word-puzzles/{puzzleId}/entries/{entryId}
        Server->>DB: SELECT WORD_PUZZLE_ENTRY + DICTIONARY
        DB-->>Server: 단서 데이터
        Server-->>Client: 단서 상세 정보
        Client-->>Admin: 단서 수정 폼 표시 (기존 값 채움)
        Admin->>Client: 값 수정 후 [저장]
        Client->>Server: PUT /api/v1/admin/word-puzzles/{puzzleId}/entries/{entryId}
        Server->>DB: UPDATE WORD_PUZZLE_ENTRY
        DB-->>Server: 수정 완료
        Server-->>Client: 수정된 단서 정보
        Client-->>Admin: 단서 목록으로 이동
    end

    alt 단서 삭제
        Admin->>Client: [삭제] 버튼 클릭
        Client-->>Admin: "삭제하시겠습니까?" 확인 다이얼로그
        Admin->>Client: 확인
        Client->>Server: DELETE /api/v1/admin/word-puzzles/{puzzleId}/entries/{entryId}
        Server->>DB: DELETE WORD_PUZZLE_ENTRY
        DB-->>Server: 삭제 완료
        Server-->>Client: 204 No Content
        Client-->>Admin: 목록 갱신
    end
```
