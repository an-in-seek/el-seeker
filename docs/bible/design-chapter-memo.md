# 장 메모(Chapter Memo) 기능 설계

`verse-list.html` 페이지에 **해당 장(Chapter) 전체에 대한 메모**를 작성·조회·수정·삭제할 수 있는 기능을 추가한다.

> 기존 구절 메모(`BibleVerseMemo`)가 절(verse) 단위라면, 장 메모는 장(chapter) 단위의 자유 메모이다.

---

## 1. 백엔드

### 1-1. Domain Entity

`BibleVerseMemo` 패턴을 따르되 `verseNumber` 필드를 제거한 장 단위 엔티티를 생성한다.

**`bible/domain/model/BibleChapterMemo.kt`**

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | PK (BaseTimeEntity) |
| member | Member | `@ManyToOne LAZY` |
| translationId | Long | 번역본 ID |
| bookOrder | Int | 책 순서 |
| chapterNumber | Int | 장 번호 |
| content | String | 메모 본문 (`TEXT`) |

- Unique 제약: `(member_id, translation_id, book_order, chapter_number)`
- `BaseTimeEntity` 상속 → `createdAt`, `updatedAt` 자동 관리
- `updateContent(content: String)` 메서드 제공

### 1-2. Repository

**`bible/adapter/output/jpa/BibleChapterMemoRepository.kt`**

```kotlin
fun findByMemberUidAndTranslationIdAndBookOrderAndChapterNumber(
    memberUid: UUID, translationId: Long, bookOrder: Int, chapterNumber: Int
): BibleChapterMemo?
```

### 1-3. Service

**`bible/application/service/BibleChapterMemoService.kt`**

| 메서드 | 설명 |
|---|---|
| `getChapterMemo(memberUid, translationId, bookOrder, chapterNumber)` | 조회 (nullable) |
| `upsertChapterMemo(member, translationId, bookOrder, chapterNumber, content)` | 존재하면 `updateContent`, 없으면 새로 생성 |
| `deleteChapterMemo(member, translationId, bookOrder, chapterNumber)` | 삭제 |

### 1-4. API Controller

**`bible/adapter/input/api/client/BibleChapterMemoApi.kt`**
**`bible/adapter/input/api/client/BibleChapterMemoApiDocument.kt`** — Swagger 어노테이션 인터페이스 (기존 `BibleChapterViewApiDocument` 패턴)

기존 경로 패턴을 따른다:

```
@RequestMapping("/api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}")
```

| Method | Path | 설명 | 응답 |
|---|---|---|---|
| `GET` | `/chapter-memo` | 장 메모 조회 | `BibleChapterMemoResponse` or `204 No Content` |
| `PUT` | `/chapter-memo` | 장 메모 생성/수정 (upsert) | `BibleChapterMemoResponse` |
| `DELETE` | `/chapter-memo` | 장 메모 삭제 | `204 No Content` |

**Request**: `BibleChapterMemoRequest`
```kotlin
data class BibleChapterMemoRequest(val content: String)
```

**Response**: `BibleChapterMemoResponse`
```kotlin
data class BibleChapterMemoResponse(
    val chapterMemoId: Long,
    val content: String,
    val updatedAt: Instant
)
```

### 1-5. ChapterState 통합

기존 `BibleChapterStateResponse`에 장 메모 필드를 추가하여 페이지 로드 시 한 번의 호출로 모든 상태를 가져온다.

```kotlin
data class BibleChapterStateResponse(
    val memos: List<BibleMemoApiResponse.MemoItem>,
    val highlights: List<BibleHighlightApiResponse.HighlightItem>,
    val isRead: Boolean,
    val chapterMemo: BibleChapterMemoResponse?   // 추가
)
```

`BibleChapterViewApi.getChapterState()`에서 `BibleChapterMemoService.getChapterMemo()` 호출을 추가한다.

---

## 2. 프론트엔드

### 2-0. 제약 사항

- **Bootstrap JS 미사용**: 이 프로젝트는 Bootstrap CSS만 WebJars로 로드하고, Bootstrap JS bundle은 포함하지 않는다. 따라서 `data-bs-toggle="modal"`, `new bootstrap.Modal()` 등 Bootstrap JS 의존 기능은 사용할 수 없다.
- **Modal 대안**: Bootstrap 클래스(`modal`, `modal-dialog` 등)를 CSS로만 활용하고, 열기/닫기는 순수 JS로 `d-none` 또는 `display` 토글 방식으로 직접 구현한다. 또는 별도 커스텀 오버레이 패널을 사용한다.
- **알림 방식**: 기존 `showAlert()`은 `window.alert()` 래퍼이다. 저장/삭제 성공 시에도 동일하게 `showAlert()`을 사용한다. (Toast 등 별도 컴포넌트 미존재)

### 2-1. HTML 수정 (`verse-list.html`)

#### 장 메모 버튼

기존 `markReadBtn` 옆에 장 메모 버튼을 추가한다. 두 버튼을 가로 배치하기 위해 `d-flex gap-2` 등으로 감싼다.

```html
<div class="text-center d-flex gap-2">
    <button id="chapterMemoBtn"
            class="btn btn-outline-secondary chapter-memo-btn flex-fill"
            type="button"
            aria-label="장 메모">
        <span id="chapterMemoIcon" aria-hidden="true">📝</span>
        <span class="ms-2">장 메모</span>
    </button>
    <button id="markReadBtn"
            class="btn btn-outline-success mark-read-btn flex-fill"
            type="button"
            aria-label="이 장 읽음 완료">
        <span id="markReadIcon" aria-hidden="true">&#x2713;</span>
        <span class="ms-2">읽음</span>
    </button>
</div>
```

- 메모 존재 시: `btn-outline-secondary` → `btn-secondary`로 변경하여 채워진 상태 표시

#### 장 메모 패널 (커스텀 오버레이)

Bootstrap JS Modal을 사용할 수 없으므로, 순수 CSS + JS로 동작하는 오버레이 패널을 구현한다. `</main>` 아래, `#verseFab` 앞에 배치한다.

```html
<div id="chapterMemoOverlay" class="chapter-memo-overlay d-none" aria-hidden="true">
    <div class="chapter-memo-panel" role="dialog" aria-labelledby="chapterMemoPanelTitle">
        <div class="chapter-memo-panel-header">
            <h5 id="chapterMemoPanelTitle" class="mb-0">장 메모</h5>
            <button type="button" class="btn-close" id="chapterMemoCloseBtn" aria-label="닫기"></button>
        </div>
        <div class="chapter-memo-panel-body">
            <textarea id="chapterMemoInput" class="form-control" rows="6"
                      placeholder="이 장에 대한 메모를 입력하세요..." aria-label="장 메모 입력"></textarea>
        </div>
        <div class="chapter-memo-panel-footer">
            <button type="button" class="btn btn-outline-danger d-none" id="chapterMemoDeleteBtn">삭제</button>
            <button type="button" class="btn btn-primary" id="chapterMemoSaveBtn">저장</button>
        </div>
    </div>
</div>
```

#### CSS 수정 (`verse-list.css`)

기존 `.mark-read-btn`의 `min-width: 100%`를 제거해야 두 버튼이 `flex-fill`로 균등 배치된다.

```css
/* 기존 */
.mark-read-btn {
    min-width: 100%;  /* 삭제 */
}
```

오버레이와 패널 스타일을 추가한다. 기존 `.memo-container` 스타일 톤과 통일한다.

```css
.chapter-memo-overlay {
    position: fixed;
    inset: 0;
    z-index: 1050;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
}

.chapter-memo-panel {
    background: #fff;
    border-radius: 0.5rem;
    width: 90%;
    max-width: 500px;
    box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.chapter-memo-panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border-bottom: 1px solid #dee2e6;
}

.chapter-memo-panel-body {
    padding: 1rem;
}

.chapter-memo-panel-footer {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    border-top: 1px solid #dee2e6;
}

.chapter-memo-btn {
    /* markReadBtn과 동일 높이 유지 */
}
```

> `verse-list.css` 수정 시 HTML의 `?v=` 파라미터를 함께 bump한다.

### 2-2. JS 수정 (`verse-list.js`)

#### 상태 추가

```javascript
const chapterMemoState = {
    memoId: null,
    content: null,
    loaded: false
};
```

#### elements에 추가

`getElements()`에 장 메모 관련 요소를 등록한다:

```javascript
chapterMemoBtn: get("chapterMemoBtn"),
chapterMemoOverlay: get("chapterMemoOverlay"),
chapterMemoInput: get("chapterMemoInput"),
chapterMemoSaveBtn: get("chapterMemoSaveBtn"),
chapterMemoDeleteBtn: get("chapterMemoDeleteBtn"),
chapterMemoCloseBtn: get("chapterMemoCloseBtn")
```

#### bindEvents()에 추가

```javascript
if (elements.chapterMemoBtn) {
    elements.chapterMemoBtn.addEventListener("click", handleChapterMemoClick);
}
if (elements.chapterMemoSaveBtn) {
    elements.chapterMemoSaveBtn.addEventListener("click", saveChapterMemo);
}
if (elements.chapterMemoDeleteBtn) {
    elements.chapterMemoDeleteBtn.addEventListener("click", deleteChapterMemo);
}
if (elements.chapterMemoCloseBtn) {
    elements.chapterMemoCloseBtn.addEventListener("click", closeChapterMemoPanel);
}
if (elements.chapterMemoOverlay) {
    elements.chapterMemoOverlay.addEventListener("click", (e) => {
        if (e.target === elements.chapterMemoOverlay) closeChapterMemoPanel();
    });
}
```

#### ESC 키 처리

기존 `handleFabEscapeKey`에 장 메모 패널 닫기 로직을 추가한다:

```javascript
// handleFabEscapeKey() 내부 — 기존 FAB 처리 전에 장 메모 패널 우선 닫기
if (!elements.chapterMemoOverlay?.classList.contains("d-none")) {
    closeChapterMemoPanel();
    return;
}
```

#### loadChapter() 내 장 메모 상태 초기화

`loadChapter()` 함수에서 장 이동 시 `chapterMemoState`도 리셋해야 한다:

```javascript
// loadChapter() 내부, memoState.cache = new Map(); 다음에 추가
chapterMemoState.memoId = null;
chapterMemoState.content = null;
chapterMemoState.loaded = false;
updateChapterMemoButton();
```

#### chapterState 통합 활용

`applyChapterState()`에서 이미 `/state` API를 호출하므로, 응답의 `chapterMemo` 필드를 `chapterMemoState`에 반영한다:

```javascript
// applyChapterState() 내부 — stateResult.data.chapterMemo 처리
if (stateResult.data.chapterMemo) {
    chapterMemoState.memoId = stateResult.data.chapterMemo.chapterMemoId;
    chapterMemoState.content = stateResult.data.chapterMemo.content;
} else {
    chapterMemoState.memoId = null;
    chapterMemoState.content = null;
}
chapterMemoState.loaded = true;
updateChapterMemoButton();
```

비인증 상태(`unauthorized`) 분기에서도 장 메모를 초기화한다:

```javascript
// applyChapterState() 내부 — unauthorized 분기에 추가
chapterMemoState.memoId = null;
chapterMemoState.content = null;
chapterMemoState.loaded = false;
updateChapterMemoButton();
```

#### 버튼 상태 표시

```javascript
function updateChapterMemoButton() {
    const btn = elements?.chapterMemoBtn;
    if (!btn) return;
    const hasMemo = Boolean(chapterMemoState.content);
    btn.classList.toggle("btn-outline-secondary", !hasMemo);
    btn.classList.toggle("btn-secondary", hasMemo);
}
```

#### 패널 열기/닫기

```javascript
function openChapterMemoPanel() {
    const overlay = elements?.chapterMemoOverlay;
    if (!overlay) return;
    elements.chapterMemoInput.value = chapterMemoState.content || "";
    elements.chapterMemoDeleteBtn.classList.toggle("d-none", !chapterMemoState.memoId);
    overlay.classList.remove("d-none");
    overlay.setAttribute("aria-hidden", "false");
}

function closeChapterMemoPanel() {
    const overlay = elements?.chapterMemoOverlay;
    if (!overlay) return;
    overlay.classList.add("d-none");
    overlay.setAttribute("aria-hidden", "true");
}
```

#### 버튼 클릭 핸들러

```javascript
async function handleChapterMemoClick() {
    if (!await ensureChapterStateReady()) {
        showAlert("사용자 상태를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", "danger");
        return;
    }
    if (!memoState.auth.allowed) {
        requestAuth(memoState.auth);
        return;
    }
    openChapterMemoPanel();
}
```

#### 저장 (PUT upsert)

```javascript
async function saveChapterMemo() {
    const content = elements.chapterMemoInput.value.trim();
    if (!content) return;
    const requestChapterKey = getCurrentChapterKey();
    try {
        const response = await fetch(buildChapterMemoUrl(), {
            method: "PUT",
            credentials: "include",
            headers: { "Content-Type": "application/json", Accept: "application/json" },
            body: JSON.stringify({ content })
        });
        if (!isCurrentChapter(requestChapterKey)) return;
        if (response.status === 401) { requestAuth(memoState.auth); return; }
        if (!response.ok) throw new Error("장 메모 저장 실패");
        const memo = await response.json();
        chapterMemoState.memoId = memo.chapterMemoId;
        chapterMemoState.content = memo.content;
        updateChapterMemoButton();
        closeChapterMemoPanel();
    } catch (error) {
        showAlert("장 메모 저장 중 오류가 발생했습니다.", "danger");
        console.error(error);
    }
}
```

#### 삭제 (DELETE)

```javascript
async function deleteChapterMemo() {
    const requestChapterKey = getCurrentChapterKey();
    try {
        const response = await fetch(buildChapterMemoUrl(), {
            method: "DELETE",
            credentials: "include"
        });
        if (!isCurrentChapter(requestChapterKey)) return;
        if (response.status === 401) { requestAuth(memoState.auth); return; }
        if (!response.ok) throw new Error("장 메모 삭제 실패");
        chapterMemoState.memoId = null;
        chapterMemoState.content = null;
        updateChapterMemoButton();
        closeChapterMemoPanel();
    } catch (error) {
        showAlert("장 메모 삭제 중 오류가 발생했습니다.", "danger");
        console.error(error);
    }
}
```

#### URL 빌더

기존 구절 메모 URL 패턴과 동일한 구조:

```javascript
function buildChapterMemoUrl() {
    return `${API_CONFIG.MEMOS_BASE}/${state.translationId}/books/${state.bookOrder}/chapters/${state.chapterNumber}/chapter-memo`;
}
```

#### 인증 패턴

기존 `memoState.auth`를 재사용한다. 구절 메모와 장 메모는 같은 인증 조건이므로 별도 authState를 만들지 않는다.

---

## 3. 구현 순서

1. **Entity + Repository** — `BibleChapterMemo`, `BibleChapterMemoRepository`
2. **Service** — `BibleChapterMemoService` (upsert/get/delete)
3. **API Controller** — `BibleChapterMemoApi` (GET/PUT/DELETE)
4. **ChapterState 확장** — `BibleChapterStateResponse`에 `chapterMemo` 필드 추가, `BibleChapterViewApi` 수정
5. **프론트엔드** — HTML(버튼 + 오버레이 패널) → CSS(패널 스타일) → JS(상태 관리 + API 연동)
6. **캐시 버스팅** — `verse-list.css?v=`, `verse-list.js?v=` 버전 bump
7. **테스트** — Service 단위 테스트 + 통합 테스트

---

## 4. 참고: 기존 패턴 대조

| 항목 | 구절 메모 (기존) | 장 메모 (신규) |
|---|---|---|
| Entity | `BibleVerseMemo` | `BibleChapterMemo` |
| Unique Key | member + translation + book + chapter + **verse** | member + translation + book + chapter |
| API 경로 | `.../verses/{verseNumber}/memo` | `.../chapter-memo` |
| HTTP Method | PUT (upsert) / DELETE | PUT (upsert) / DELETE |
| State 통합 | `chapterState.memos[]` | `chapterState.chapterMemo` |
| 인증 | `memoState.auth` | `memoState.auth` (공유) |
| UI | 인라인 textarea (구절 하단) | 커스텀 오버레이 패널 (Bootstrap JS 미사용) |
| 알림 | `showAlert()` → `window.alert()` | 동일 |
| 장 이동 초기화 | `memoState.cache = new Map()` | `chapterMemoState` 리셋 |
