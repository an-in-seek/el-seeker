# 성경 가로세로 낱말 퍼즐 화면 설계서 (UI/UX Spec)

---

## 1. 화면 구조 (Information Architecture)

- **[SCR-01] 퍼즐 목록 화면:** 테마/난이도별 퍼즐 탐색 및 진입
- **[SCR-02] 퍼즐 플레이 화면:** 메인 게임 보드 및 단서 입력
- **[SCR-03] 퍼즐 결과 화면:** 점수 확인 및 성경 단어 학습

---

## 2. 화면별 상세 설계

### [SCR-01] 퍼즐 목록 화면 (Puzzle List)

**1. 화면 목적**
사용자가 풀고 싶은 퍼즐을 검색하고, 새로운 퍼즐을 시작하거나 진행 중이던 퍼즐을 이어할 수 있도록 목록을 제공합니다.

**2. UI 컴포넌트 구성**

* **Header:** 서비스 로고, 내 프로필(또는 마이페이지) 버튼
* **Filter Bar:**
    * 테마 드롭다운 (전체 / 창세기 / 십계명 / 사복음서 등)
    * 난이도 드롭다운 (전체 / 쉬움 / 보통 / 어려움)
* **Puzzle List (Card View):**
    * **퍼즐 카드 정보:** 퍼즐 제목, 게시일, 보드 크기(예: 10x10)
    * **상태 뱃지:** `새로운 퍼즐` / `진행 중`
* **로딩/에러 상태:**
    * 목록 로딩 중: 스켈레톤 카드 UI 표시
    * 로딩 실패: "퍼즐 목록을 불러올 수 없습니다. 다시 시도해 주세요." 메시지 + 재시도 버튼

**3. 인터랙션 및 API 매핑**

* 화면 진입 시 및 필터 변경 시 `GET /api/v1/game/word-puzzles` API 호출
* 카드 탭 시:
    * 상태가 '새로운 퍼즐'인 경우: `POST /api/v1/game/word-puzzles/{puzzleId}/attempts` 호출 후 **[SCR-02]** 로 이동
    * 상태가 '진행 중'인 경우: `GET /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}` 호출 후 **[SCR-02]** 로 이동

---

### [SCR-02] 퍼즐 플레이 화면 (Puzzle Play)

**1. 화면 목적**
실제 낱말 퍼즐을 푸는 메인 게임 화면으로, 잦은 입력과 수정이 일어나는 핵심 화면입니다.

**2. UI 컴포넌트 구성**

* **Top Navigation Bar:**
    * 뒤로 가기 버튼 (목록으로 이동 시 타이머 일시정지)
    * 퍼즐 제목
    * **타이머 (00:00 형식, 실시간 증가)**
    * 더보기 버튼 (옵션: 목록으로 돌아가기)
* **Puzzle Board (Grid):**
    * 빈 칸, 막힌 칸(블랙 블록), 입력된 글자 표시
    * 칸 좌측 상단에 단서 번호(Clue Number) 작게 표시
    * **상태 시각화:**
        * 선택된 칸 (강조색 적용)
        * 선택된 단어 범위 (옅은 강조색 적용)
        * 힌트로 열린 칸 (아이콘 또는 텍스트 색상 차별화)
* **Clue Bar (Active Clue):**
    * 보드 바로 아래에 현재 선택된 단어의 힌트(가로/세로 방향, 텍스트)를 스와이퍼 형태로 고정 노출
* **Action Toolbar & Keyboard:**
    * **힌트 버튼 1 (글자 공개):** 돋보기 아이콘 (사용 시 감점 안내 툴팁)
    * **힌트 버튼 2 (단어 확인):** 체크마크 아이콘
    * 가상 키보드 (모바일 환경인 경우) / PC인 경우 물리 키보드 매핑
    * **전체 제출 버튼:** 보드가 모두 채워졌을 때만 활성화 (또는 하이라이트)
* **로딩/에러 상태:**
    * 자동 저장 실패: 화면 상단에 "저장 실패 — 네트워크를 확인해 주세요" 배너 표시. 네트워크 복구 시 자동 재시도
    * 힌트 API 실패: "힌트를 불러올 수 없습니다." 토스트 메시지
    * 제출 API 실패: "제출에 실패했습니다. 다시 시도해 주세요." 토스트 메시지 + 제출 버튼 재활성화

**3. 키보드 매핑**

| 키 | 동작 |
|---|---|
| 한글 문자키 | 현재 칸에 글자 입력 후 다음 칸으로 자동 이동 |
| Backspace | 현재 칸의 글자 삭제. 현재 칸이 비어있으면 이전 칸으로 이동 후 해당 칸의 글자를 삭제 |
| 방향키 (←→↑↓) | 칸 이동 (방향에 따라) |
| Tab | 다음 단서의 첫 번째 칸으로 이동 |
| Shift + Tab | 이전 단서의 첫 번째 칸으로 이동 |
| Space 또는 칸 재클릭 | 교차점에서 가로 ↔ 세로 방향 전환 |

**4. 한글 입력(IME) 처리**

* 한 칸에 완성된 한 글자만 입력 가능 (예: "창")
* 한글 조합 중(ㅊ→차→창) 상태에서는 조합 완료 시점에 셀에 반영
* `compositionend` 이벤트를 기준으로 최종 글자를 확정하고, 확정된 글자만 자동 저장 대상에 포함
* 조합 중에는 셀에 미완성 글자를 임시 표시 (옅은 색상)

**5. 인터랙션 및 API 매핑**

* **칸 선택:** 보드 터치 시 가로/세로 방향 토글 및 해당 `Clue Bar` 텍스트 변경
* **글자 입력/삭제:** 입력 시 다음 칸으로 자동 포커스 이동. 내부 스토어(상태) 업데이트 후 디바운스(Debounce) 처리하여 `PATCH /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/cells` 백그라운드 호출
* **힌트 사용:** 버튼 탭 시 모달(확인 창) 노출 후 `POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/hints/reveal-letter` 또는 `hints/check-word` 호출
* **제출:** 빈 칸이 존재하면 전체 제출 버튼 비활성화. 모든 칸 입력 시 버튼 활성화되며, 탭 시 '제출하시겠습니까?' 토스트 팝업 노출. 확인 시 `POST /api/v1/game/word-puzzles/{puzzleId}/attempts/{attemptId}/submit` 호출

---

### [SCR-03] 퍼즐 결과 및 학습 화면 (Puzzle Result & Study)

**1. 화면 목적**
퍼즐을 완료한 사용자에게 성취감을 부여하고, 게임 중 등장했던 성경 단어들의 깊이 있는 학습 정보를 제공합니다.

**2. UI 컴포넌트 구성**

* **Result Header:**
    * "퍼즐 완료!" 축하 애니메이션 (Lottie 등 활용)
* **Score Board:**
    * 최종 점수 (크게 강조)
    * 소요 시간, 힌트 사용 횟수, 오답 제출 횟수 (감점 내역 상세 표시)
* **Word Study List (학습 영역):**
    * 퍼즐에 사용된 정답 단어 목록 아코디언(Accordion) 또는 리스트 형태로 제공
    * **단어 카드 구성:**
        * 단어 원형 (예: 창조)
        * 원어 정보 (예: בָּרָא - 히브리어)
        * 사전적 정의 (예: 하나님이 세상을 만드신 행위)
        * 관련 성경 구절 박스 (예: 창세기 1:1)
* **Bottom Actions:**
    * [다른 퍼즐 풀기] 버튼 (목록으로 이동)
    * [결과 공유하기] 버튼 (Web Share API 활용. 미지원 브라우저에서는 클립보드 복사 fallback)

**3. 인터랙션 및 API 매핑**

* **[SCR-02]** 에서 정답 제출 API의 응답 데이터를 받아 화면에 렌더링
* (오답일 경우 이 화면으로 넘어오지 않고 **[SCR-02]** 에 머물며 틀린 칸을 빨간색으로 깜빡임 처리)
* [다른 퍼즐 풀기] 탭 시 라우터를 통해 **[SCR-01]** 로 이동

---

## 3. 관리자 화면 설계

> 관리자 화면은 `/web/admin` 하위 경로에서 접근하며, `ADMIN` 역할 인증이 필요하다.

### [ADM-01] 퍼즐 목록 화면 (Admin Puzzle List)

**1. 화면 목적**
관리자가 전체 퍼즐을 조회하고 상태(DRAFT / PUBLISHED / ARCHIVED)를 관리할 수 있는 목록 화면.

**2. 진입 경로**
`GET /web/admin/word-puzzles`

**3. UI 컴포넌트 구성**

* **Admin Sidebar:** 기존 관리자 사이드바에 "성경 Word Puzzle" 메뉴 항목 추가
* **Admin Toolbar:**
    * [등록] 버튼 → **[ADM-02]** 로 이동
* **Puzzle Table (데스크톱) / Card List (모바일):**
    * 컬럼: ID, 제목, 테마, 난이도, 보드 크기, 상태, 게시일, 작업
    * **상태 뱃지:** `DRAFT` (회색), `PUBLISHED` (초록), `ARCHIVED` (주황)
    * **작업 버튼:**
        * [수정] → **[ADM-02]** (편집 모드)
        * [단서 관리] → **[ADM-03]**
        * [게시] / [비공개] — 상태 전환 버튼 (현재 상태에 따라 동적 노출)
        * [삭제] — DRAFT 상태에서만 노출. 확인 다이얼로그 후 삭제
* **Pagination:** 기본 20건 단위 페이지네이션

**4. 인터랙션 및 API 매핑**

| 인터랙션 | API |
|---|---|
| 화면 진입 | `GET /api/v1/admin/word-puzzles` |
| [게시] 클릭 (DRAFT / ARCHIVED 상태) | `PATCH /api/v1/admin/word-puzzles/{id}/status` `{ "status": "PUBLISHED" }` |
| [비공개] 클릭 (PUBLISHED 상태) | `PATCH /api/v1/admin/word-puzzles/{id}/status` `{ "status": "ARCHIVED" }` |
| [삭제] 클릭 | `DELETE /api/v1/admin/word-puzzles/{id}` |

---

### [ADM-02] 퍼즐 등록/수정 화면 (Admin Puzzle Form)

**1. 화면 목적**
퍼즐의 기본 정보(제목, 테마, 난이도, 보드 크기)를 등록하거나 수정한다.

**2. 진입 경로**
* 등록: `GET /web/admin/word-puzzles/new`
* 수정: `GET /web/admin/word-puzzles/{id}/edit`

**3. UI 컴포넌트 구성**

* **Breadcrumb:** `Puzzle 목록 > 등록` 또는 `Puzzle 목록 > 수정`
* **Form Fields:**

| 필드 | 입력 유형 | 필수 | 설명 |
|---|---|---|---|
| 제목 | text input | Y | 퍼즐 제목 (최대 200자) |
| 테마 코드 | text input | Y | 테마 코드 (예: GENESIS, TEN_COMMANDMENTS) |
| 난이도 | select | Y | EASY / NORMAL / HARD |
| 보드 가로 크기 | number input | Y | 보드 열 수 (5~20) |
| 보드 세로 크기 | number input | Y | 보드 행 수 (5~20) |

* **Action Buttons:**
    * [저장] — API 호출 후 **[ADM-01]** 로 이동
    * [취소] — **[ADM-01]** 로 이동

**4. 인터랙션 및 API 매핑**

| 인터랙션 | API |
|---|---|
| 수정 화면 진입 | `GET /api/v1/admin/word-puzzles/{id}` |
| [저장] — 등록 | `POST /api/v1/admin/word-puzzles` |
| [저장] — 수정 | `PUT /api/v1/admin/word-puzzles/{id}` |

---

### [ADM-03] 퍼즐 단서(Entry) 관리 화면 (Admin Entry List)

**1. 화면 목적**
특정 퍼즐에 속하는 단서(entry)를 조회, 추가, 수정, 삭제한다. 각 단서는 사전(Dictionary) 단어와 연결된다.

**2. 진입 경로**
`GET /web/admin/word-puzzles/{puzzleId}/entries`

**3. UI 컴포넌트 구성**

* **Breadcrumb:** `Puzzle 목록 > {퍼즐 제목} > 단서 관리`
* **Puzzle Summary:** 퍼즐 제목, 보드 크기, 난이도, 상태를 요약 표시
* **Admin Toolbar:**
    * [단서 추가] 버튼 → **[ADM-04]** 로 이동
* **Entry Table (데스크톱) / Card List (모바일):**
    * 컬럼: ID, 번호, 방향, 사전 단어, 정답, 단서 유형, 단서 텍스트, 시작 위치, 작업
    * **방향 뱃지:** `ACROSS` (파랑), `DOWN` (보라)
    * **작업 버튼:**
        * [수정] → **[ADM-04]** (편집 모드)
        * [삭제] — 확인 다이얼로그 후 삭제
* **Pagination:** 기본 50건 단위

**4. 인터랙션 및 API 매핑**

| 인터랙션 | API |
|---|---|
| 화면 진입 | `GET /api/v1/admin/word-puzzles/{puzzleId}/entries` |
| [삭제] 클릭 | `DELETE /api/v1/admin/word-puzzles/{puzzleId}/entries/{entryId}` |

---

### [ADM-04] 단서(Entry) 등록/수정 화면 (Admin Entry Form)

**1. 화면 목적**
퍼즐의 단서(entry)를 등록하거나 수정한다. 사전(Dictionary) 단어 검색 후 연결하고, 보드 배치 정보와 단서 텍스트를 입력한다.

**2. 진입 경로**
* 등록: `GET /web/admin/word-puzzles/{puzzleId}/entries/new`
* 수정: `GET /web/admin/word-puzzles/{puzzleId}/entries/{entryId}/edit`

**3. UI 컴포넌트 구성**

* **Breadcrumb:** `Puzzle 목록 > {퍼즐 제목} > 단서 관리 > 등록/수정`
* **Dictionary Search:**
    * 검색 input + [검색] 버튼 → `GET /api/v1/admin/word-puzzles/dictionaries?term={keyword}` 호출
    * 검색 결과를 드롭다운 목록으로 표시 (term, description 요약)
    * 항목 클릭 시 해당 Dictionary를 선택. 선택된 단어 정보(term, description, 원어) 표시
* **Form Fields:**

| 필드 | 입력 유형 | 필수 | 설명 |
|---|---|---|---|
| 사전 단어 | 검색+선택 | Y | Dictionary 검색하여 선택 |
| 정답 텍스트 | text input | Y | 보드에 배치될 순수 정답 문자열 (공백 제거) |
| 방향 | select | Y | ACROSS / DOWN |
| 시작 행 | number input | Y | 0-based 행 인덱스 |
| 시작 열 | number input | Y | 0-based 열 인덱스 |
| 단서 번호 | number input | Y | 보드에 표시될 단서 번호 |
| 단서 유형 | select | Y | DEFINITION / VERSE |
| 단서 텍스트 | textarea | Y | 플레이어에게 보여줄 힌트 문장 |

* **Action Buttons:**
    * [저장] — API 호출 후 **[ADM-03]** 으로 이동
    * [취소] — **[ADM-03]** 으로 이동

**4. 인터랙션 및 API 매핑**

| 인터랙션 | API |
|---|---|
| 수정 화면 진입 | `GET /api/v1/admin/word-puzzles/{puzzleId}/entries/{entryId}` |
| 사전 검색 | `GET /api/v1/admin/word-puzzles/dictionaries?term={keyword}` |
| [저장] — 등록 | `POST /api/v1/admin/word-puzzles/{puzzleId}/entries` |
| [저장] — 수정 | `PUT /api/v1/admin/word-puzzles/{puzzleId}/entries/{entryId}` |
