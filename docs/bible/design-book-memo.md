# 책 메모(Book Memo) 기능 설계

`chapter-list.html` 페이지에 **해당 책(Book) 전체에 대한 메모**를 작성·조회·수정·삭제할 수 있는 기능을 추가한다.

> 기존 장 메모(`BibleChapterMemo`)가 장(chapter) 단위라면, 책 메모는 책(book) 단위의 자유 메모이다.

---

## 1. 백엔드

### 1-1. Domain Entity

`BibleChapterMemo` 패턴을 따르되 `chapterNumber` 필드를 제거한 책 단위 엔티티를 생성한다.

**`bible/domain/model/BibleBookMemo.kt`**

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | PK (BaseTimeEntity) |
| member | Member | `@ManyToOne LAZY` |
| translationId | Long | 번역본 ID |
| bookOrder | Int | 책 순서 |
| content | String | 메모 본문 (`TEXT`) |

- Unique 제약: `(member_id, translation_id, book_order)`
- `BaseTimeEntity` 상속 → `createdAt`, `updatedAt` 자동 관리 (`Instant` 타입, non-nullable)
- `updateContent(content: String)` 메서드 제공

### 1-2. Repository

**`bible/adapter/output/jpa/BibleBookMemoRepository.kt`**

```kotlin
fun findByMemberUidAndTranslationIdAndBookOrder(
    memberUid: UUID, translationId: Long, bookOrder: Int
): BibleBookMemo?

fun findByMemberAndTranslationIdAndBookOrder(
    member: Member, translationId: Long, bookOrder: Int
): BibleBookMemo?

fun deleteAllByMember(member: Member)
```

- `deleteAllByMember`: 회원 탈퇴 시 데이터 정리용 (기존 `BibleChapterMemoRepository` 동일 패턴)

### 1-3. Service

**`bible/application/service/BibleBookMemoService.kt`**

| 메서드 | 설명 |
|---|---|
| `getBookMemo(memberUid, translationId, bookOrder)` | 조회 (nullable) |
| `upsertBookMemo(member, translationId, bookOrder, content)` | 존재하면 `updateContent`, 없으면 새로 생성 |
| `deleteBookMemo(member, translationId, bookOrder)` | 삭제 |

- `upsertBookMemo`에서 `content.trim()` 후 빈 값이면 `ErrorType.INVALID_PARAMETER` 예외 발생 (기존 `BibleChapterMemoService` 패턴 동일)

### 1-4. API Controller

**`bible/adapter/input/api/client/BibleBookMemoApi.kt`**
**`bible/adapter/input/api/client/BibleBookMemoApiDocument.kt`** — Swagger 어노테이션 인터페이스

기존 경로 패턴을 따른다:

```
@RequestMapping("/api/v1/bibles/translations/{translationId}/books/{bookOrder}")
```

| Method | Path | 설명 | 응답 |
|---|---|---|---|
| `GET` | `/book-memo` | 책 메모 조회 | `BibleBookMemoApiResponse.BookMemoItem` or `204 No Content` |
| `PUT` | `/book-memo` | 책 메모 생성/수정 (upsert) | `BibleBookMemoApiResponse.BookMemoItem` |
| `DELETE` | `/book-memo` | 책 메모 삭제 | `204 No Content` |

**Request**: `BibleBookMemoRequest`
```kotlin
data class BibleBookMemoRequest(val content: String)
```

**Response**: `BibleBookMemoApiResponse`
```kotlin
class BibleBookMemoApiResponse {
    data class BookMemoItem(
        val bookMemoId: Long,
        val content: String,
        val updatedAt: Instant
    ) {
        companion object {
            fun from(memo: BibleBookMemo) = BookMemoItem(
                bookMemoId = memo.id ?: 0,
                content = memo.content,
                updatedAt = memo.updatedAt
            )
        }
    }
}
```

> `BaseTimeEntity`의 `updatedAt`은 `Instant` (non-nullable)이므로 `!!` 없이 직접 참조한다. `id`는 영속 전 `null` 가능성이 있으므로 `?: 0`으로 처리한다. (기존 `BibleChapterMemoApiResponse` 패턴 동일)

### 1-5. SecurityConfig 수정

**`common/security/SecurityConfig.kt`**

현재 `/api/v1/bibles/**`가 `permitAll()`로 설정되어 있어, 명시적으로 `.authenticated()` 블록에 책 메모 경로를 추가해야 한다.

```kotlin
// 기존 authenticated 블록 (line 79~85)에 추가
.requestMatchers(
    // ... 기존 chapter 관련 경로들 ...
    "/api/v1/bibles/translations/{translationId}/books/{bookOrder}/chapters/{chapterNumber}/state",
    "/api/v1/bibles/translations/{translationId}/books/{bookOrder}/book-memo"  // 추가
).authenticated()
```

> 참고: 기존 장 메모 경로(`/chapter-memo`)도 동일하게 `.authenticated()` 블록에 누락되어 있다. 책 메모 구현 시 장 메모 경로도 함께 추가하는 것을 권장한다.

### 1-6. 회원 탈퇴 시 데이터 정리

**`member/application/service/MemberService.kt`** — `withdrawMember()` 메서드에 추가
**`member/application/service/AdminMemberService.kt`** — `deleteMember()` 메서드에 추가

```kotlin
// 기존 bibleMemoRepository.deleteAllByMember(member) 다음에 추가
bibleBookMemoRepository.deleteAllByMember(member)
```

> 참고: 기존 `BibleChapterMemo`도 `MemberService.withdrawMember()` 및 `AdminMemberService.deleteMember()`에서 `deleteAllByMember` 호출이 누락되어 있다. 책 메모 구현 시 장 메모 정리 코드도 함께 추가하는 것을 권장한다:
> ```kotlin
> bibleChapterMemoRepository.deleteAllByMember(member)
> bibleBookMemoRepository.deleteAllByMember(member)
> ```

---

## 2. 프론트엔드

### 2-0. 제약 사항

- **Bootstrap JS 미사용**: Bootstrap CSS만 WebJars로 로드하고, Bootstrap JS bundle은 포함하지 않는다. Modal 등 Bootstrap JS 의존 기능은 사용할 수 없다.
- **Modal 대안**: Bootstrap 클래스를 CSS로만 활용하고, 열기/닫기는 순수 JS로 `d-none` 토글 방식으로 직접 구현한다. `d-none` 제거 시 CSS의 `display: flex`가 적용되어 오버레이 중앙 정렬이 동작한다.
- **알림 방식**: `window.alert()` 사용 (기존 패턴 동일).

### 2-1. HTML 수정 (`chapter-list.html`)

#### 책 메모 버튼

기존 `book-action-buttons` 영역에 책 메모 버튼을 추가한다. 기존 "개요 영상", "퀴즈" 버튼과 동일한 스타일의 액션 버튼으로 배치한다.

```html
<div class="book-action-buttons">
    <a id="overviewVideoBtn"
       class="book-action-btn"
       href="#"
       aria-label="개요 영상 보기">
        <span class="book-action-btn-icon" aria-hidden="true">▶️</span>
        <span class="book-action-btn-label">개요 영상</span>
    </a>
    <a id="gameBtn"
       class="book-action-btn"
       href="/web/game/bible-ox-quiz/map"
       aria-label="O/X 퀴즈 게임">
        <span class="book-action-btn-icon" aria-hidden="true">🎮</span>
        <span class="book-action-btn-label">퀴즈</span>
    </a>
    <!-- 추가 -->
    <button id="bookMemoBtn"
            class="book-action-btn"
            type="button"
            aria-label="책 메모">
        <span id="bookMemoIcon" class="book-action-btn-icon" aria-hidden="true">📝</span>
        <span class="book-action-btn-label">메모</span>
    </button>
</div>
```

- 메모 존재 시: `book-action-btn-active` 클래스를 추가하여 배경색 변경으로 채워진 상태 표시
- `<button>` 태그 사용 (기존 `<a>` 태그와 달리 페이지 이동이 아닌 JS 동작이므로)

#### 책 메모 패널 (커스텀 오버레이)

Bootstrap JS Modal을 사용할 수 없으므로, 장 메모 패널(`chapterMemoOverlay`)과 동일한 순수 CSS + JS 방식의 오버레이 패널을 구현한다. `</main>` 아래, 하단 네비게이션(`<div class="fixed-bottom-nav">`) 앞에 배치한다.

```html
<div id="bookMemoOverlay" class="book-memo-overlay d-none" aria-hidden="true">
    <div class="book-memo-panel" role="dialog" aria-labelledby="bookMemoPanelTitle">
        <div class="book-memo-panel-header">
            <h5 id="bookMemoPanelTitle" class="mb-0">책 메모</h5>
            <button type="button" class="btn-close" id="bookMemoCloseBtn" aria-label="닫기"></button>
        </div>
        <div class="book-memo-panel-body">
            <textarea id="bookMemoInput" class="form-control" rows="6"
                      placeholder="이 책에 대한 메모를 입력하세요..." aria-label="책 메모 입력"></textarea>
        </div>
        <div class="book-memo-panel-footer">
            <button type="button" class="btn btn-outline-danger d-none" id="bookMemoDeleteBtn">삭제</button>
            <button type="button" class="btn btn-primary" id="bookMemoSaveBtn">저장</button>
        </div>
    </div>
</div>
```

### 2-2. CSS 수정 (`chapter-list.css`)

#### 3개 버튼 배치 시 border-radius 수정

기존 CSS는 2개 버튼 기준으로 `:first-child`(왼쪽 하단 라운드), `:last-child`(오른쪽 하단 라운드)만 설정되어 있다. 3번째 버튼 추가 시 중간 버튼의 `border-right` 처리가 필요하다.

```css
/* 기존 규칙 수정 */
.book-action-btn:first-child {
    border-radius: 0 0 0 6px;
    border-right: none;
}

/* 추가: 중간 버튼 (첫 번째도 마지막도 아닌 버튼) */
.book-action-btn:not(:first-child):not(:last-child) {
    border-radius: 0;
    border-right: none;
}

.book-action-btn:last-child {
    border-radius: 0 0 6px 0;
}
```

#### `<button>` 태그 스타일 정규화

기존 `book-action-btn`은 `<a>` 태그 기준이다. `<button>` 태그는 브라우저 기본 스타일(font-family, line-height 등)이 다르므로 정규화한다.

```css
button.book-action-btn {
    font-family: inherit;
    font-size: inherit;
    line-height: inherit;
}
```

#### 메모 버튼 활성 상태

```css
.book-action-btn-active {
    background-color: #d1e7dd;
    border-color: #badbcc;
    color: #0f5132;
}
```

#### 오버레이 패널 스타일

장 메모의 `.chapter-memo-overlay` 스타일과 동일한 톤으로 추가한다.

```css
.book-memo-overlay {
    position: fixed;
    inset: 0;
    z-index: 1050;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
}

.book-memo-panel {
    background: #fff;
    border-radius: 0.5rem;
    width: 90%;
    max-width: 500px;
    box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}

.book-memo-panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border-bottom: 1px solid #dee2e6;
}

.book-memo-panel-body {
    padding: 1rem;
}

.book-memo-panel-footer {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    border-top: 1px solid #dee2e6;
}
```

> 캐시 버스팅: `chapter-list.css?v=2.3` → `?v=2.4`, `chapter-list.js?v=2.6` → `?v=2.7`로 bump한다.

### 2-3. JS 수정 (`chapter-list.js`)

#### 상태 추가

```javascript
const bookMemoState = {
    memoId: null,
    content: null,
    loaded: false
};
```

#### DomHelper.getElements()에 추가

```javascript
bookMemoBtn: get("bookMemoBtn"),
bookMemoOverlay: get("bookMemoOverlay"),
bookMemoInput: get("bookMemoInput"),
bookMemoSaveBtn: get("bookMemoSaveBtn"),
bookMemoDeleteBtn: get("bookMemoDeleteBtn"),
bookMemoCloseBtn: get("bookMemoCloseBtn")
```

#### 인증 체크 후 메모 조회

기존 `App.initAuthStatus()` 완료 후 이벤트를 바인딩하고 메모를 조회한다. 메모 조회는 장 목록 렌더링과 독립적이므로 `await` 없이 비동기로 실행하여 렌더링을 블로킹하지 않는다.

```javascript
// App.init() 내부 — await App.initAuthStatus(); 다음에 추가
App.bindBookMemoEvents();
App.loadBookMemo();  // await 없이 비동기 실행 — 렌더링 블로킹 방지
```

#### 이벤트 바인딩

```javascript
bindBookMemoEvents: () => {
    const el = App.elements;
    if (el.bookMemoBtn) {
        el.bookMemoBtn.addEventListener("click", App.handleBookMemoClick);
    }
    if (el.bookMemoSaveBtn) {
        el.bookMemoSaveBtn.addEventListener("click", App.saveBookMemo);
    }
    if (el.bookMemoDeleteBtn) {
        el.bookMemoDeleteBtn.addEventListener("click", App.deleteBookMemo);
    }
    if (el.bookMemoCloseBtn) {
        el.bookMemoCloseBtn.addEventListener("click", App.closeBookMemoPanel);
    }
    if (el.bookMemoOverlay) {
        el.bookMemoOverlay.addEventListener("click", (e) => {
            if (e.target === el.bookMemoOverlay) App.closeBookMemoPanel();
        });
    }
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !el.bookMemoOverlay?.classList.contains("d-none")) {
            App.closeBookMemoPanel();
        }
    });
},
```

#### 책 메모 조회

```javascript
loadBookMemo: async () => {
    if (!App.isAuthenticated) {
        bookMemoState.memoId = null;
        bookMemoState.content = null;
        bookMemoState.loaded = false;
        App.updateBookMemoButton();
        return;
    }
    try {
        const url = App.buildBookMemoUrl();
        const response = await fetch(url, {
            method: "GET",
            credentials: "include",
            headers: { Accept: "application/json" }
        });
        if (response.status === 204) {
            bookMemoState.memoId = null;
            bookMemoState.content = null;
        } else if (response.ok) {
            const memo = await response.json();
            bookMemoState.memoId = memo.bookMemoId;
            bookMemoState.content = memo.content;
        } else if (response.status === 401) {
            App.isAuthenticated = false;
            bookMemoState.memoId = null;
            bookMemoState.content = null;
        }
    } catch (error) {
        console.warn("책 메모 조회 실패:", error.message);
    }
    bookMemoState.loaded = true;
    App.updateBookMemoButton();
},
```

#### 버튼 상태 표시

```javascript
updateBookMemoButton: () => {
    const btn = App.elements?.bookMemoBtn;
    if (!btn) return;
    const hasMemo = Boolean(bookMemoState.content);
    btn.classList.toggle("book-action-btn-active", hasMemo);
},
```

#### 패널 열기/닫기

```javascript
openBookMemoPanel: () => {
    const overlay = App.elements?.bookMemoOverlay;
    if (!overlay) return;
    App.elements.bookMemoInput.value = bookMemoState.content || "";
    App.elements.bookMemoDeleteBtn.classList.toggle("d-none", !bookMemoState.memoId);
    overlay.classList.remove("d-none");
    overlay.setAttribute("aria-hidden", "false");
},

closeBookMemoPanel: () => {
    const overlay = App.elements?.bookMemoOverlay;
    if (!overlay) return;
    overlay.classList.add("d-none");
    overlay.setAttribute("aria-hidden", "true");
},
```

#### 버튼 클릭 핸들러

```javascript
handleBookMemoClick: () => {
    if (!App.isAuthenticated) {
        const currentUrl = window.location.pathname + window.location.search;
        window.location.href = `/web/auth/login?returnUrl=${encodeURIComponent(currentUrl)}`;
        return;
    }
    App.openBookMemoPanel();
},
```

#### 저장 (PUT upsert)

```javascript
saveBookMemo: async () => {
    const content = App.elements.bookMemoInput.value.trim();
    if (!content) return;
    try {
        const response = await fetch(App.buildBookMemoUrl(), {
            method: "PUT",
            credentials: "include",
            headers: { "Content-Type": "application/json", Accept: "application/json" },
            body: JSON.stringify({ content })
        });
        if (response.status === 401) {
            window.location.href = `/web/auth/login?returnUrl=${encodeURIComponent(window.location.pathname + window.location.search)}`;
            return;
        }
        if (!response.ok) throw new Error("책 메모 저장 실패");
        const memo = await response.json();
        bookMemoState.memoId = memo.bookMemoId;
        bookMemoState.content = memo.content;
        App.updateBookMemoButton();
        App.closeBookMemoPanel();
    } catch (error) {
        alert("책 메모 저장 중 오류가 발생했습니다.");
        console.error(error);
    }
},
```

#### 삭제 (DELETE)

```javascript
deleteBookMemo: async () => {
    try {
        const response = await fetch(App.buildBookMemoUrl(), {
            method: "DELETE",
            credentials: "include"
        });
        if (response.status === 401) {
            window.location.href = `/web/auth/login?returnUrl=${encodeURIComponent(window.location.pathname + window.location.search)}`;
            return;
        }
        if (!response.ok) throw new Error("책 메모 삭제 실패");
        bookMemoState.memoId = null;
        bookMemoState.content = null;
        App.updateBookMemoButton();
        App.closeBookMemoPanel();
    } catch (error) {
        alert("책 메모 삭제 중 오류가 발생했습니다.");
        console.error(error);
    }
},
```

#### URL 빌더

```javascript
buildBookMemoUrl: () => {
    return `${API_CONFIG.TRANSLATIONS}/${App.state.translationId}/books/${App.state.bookOrder}/book-memo`;
},
```

#### 책 이동 시 메모 상태 초기화

`navigateToBook()` 함수에서 책 이동 시 `bookMemoState`를 리셋하고 새로 조회한다. 렌더링(`renderFromSessionStorage` / `fetchChaptersFromAPI`) 이후에 배치하여 장 목록 표시를 블로킹하지 않는다.

```javascript
// navigateToBook() 내부 — renderFromSessionStorage / fetchChaptersFromAPI 이후에 추가
bookMemoState.memoId = null;
bookMemoState.content = null;
bookMemoState.loaded = false;
App.updateBookMemoButton();
App.loadBookMemo();  // await 없이 비동기 실행
```

`popstate` 이벤트 핸들러에도 동일하게 추가한다. 렌더링 블록(`renderFromSessionStorage` / `fetchChaptersFromAPI`) 이후에 배치한다:

```javascript
// popstate 핸들러 내부 — renderFromSessionStorage / fetchChaptersFromAPI 이후 (마지막)에 추가
App.loadBookMemo();  // await 없이 비동기 실행
```

---

## 3. 구현 순서

1. **Entity + Repository** — `BibleBookMemo`, `BibleBookMemoRepository` (`deleteAllByMember` 포함)
2. **Service** — `BibleBookMemoService` (upsert/get/delete)
3. **API Controller** — `BibleBookMemoApi`, `BibleBookMemoApiDocument` (GET/PUT/DELETE)
4. **Request/Response DTO** — `BibleBookMemoRequest`, `BibleBookMemoApiResponse`
5. **SecurityConfig** — `.authenticated()` 블록에 `/book-memo` 경로 추가
6. **회원 탈퇴 정리** — `MemberService`, `AdminMemberService`에 `bibleBookMemoRepository.deleteAllByMember()` 추가
7. **프론트엔드** — HTML(메모 버튼 + 오버레이 패널) → CSS(3버튼 border-radius + 패널 스타일) → JS(상태 관리 + API 연동)
8. **캐시 버스팅** — `chapter-list.css?v=2.3` → `?v=2.4`, `chapter-list.js?v=2.6` → `?v=2.7`
9. **테스트** — Service 단위 테스트 + 통합 테스트

---

## 4. 참고: 기존 패턴 대조

| 항목 | 장 메모 (기존) | 책 메모 (신규) |
|---|---|---|
| Entity | `BibleChapterMemo` | `BibleBookMemo` |
| Unique Key | member + translation + book + **chapter** | member + translation + book |
| API 경로 | `.../chapters/{chapterNumber}/chapter-memo` | `.../books/{bookOrder}/book-memo` |
| HTTP Method | GET / PUT (upsert) / DELETE | GET / PUT (upsert) / DELETE |
| 대상 화면 | `verse-list.html` | `chapter-list.html` |
| 인증 | `memoState.auth` (chapterState 통합) | `App.isAuthenticated` (기존 auth 활용) |
| UI | 커스텀 오버레이 패널 | 동일 패턴 (커스텀 오버레이 패널) |
| 버튼 위치 | `markReadBtn` 옆 별도 버튼 | `book-action-buttons` 내 액션 버튼 (3번째) |
| 메모 조회 시점 | `chapterState` API 통합 조회 | 별도 GET 호출 (init 시, 비동기) |
| 책/장 이동 초기화 | `chapterMemoState` 리셋 | `bookMemoState` 리셋 + 비동기 재조회 |
| 알림 | `showAlert()` → `window.alert()` | `window.alert()` 직접 사용 |

---

## 5. 기존 코드 버그 참고 (함께 수정 권장)

책 메모 구현 시 발견된 기존 코드의 누락 사항으로, 함께 수정하면 좋다.

| 항목 | 현황 | 권장 수정 |
|---|---|---|
| `SecurityConfig` — 장 메모 경로 | `/chapter-memo` 경로가 `.authenticated()` 블록에 미등록 (현재 `/api/v1/bibles/**` permitAll에 매칭됨) | `.authenticated()` 블록에 `/chapter-memo` 경로 추가 |
| `MemberService.withdrawMember()` — 장 메모 정리 | `bibleChapterMemoRepository.deleteAllByMember()` 호출 누락 | 회원 탈퇴 시 장 메모 삭제 코드 추가 |
| `AdminMemberService.deleteMember()` — 장 메모 정리 | 동일하게 누락 | 동일하게 추가 |
