# 마이페이지 UI/UX 개선 구현 계획

## 현재 상태 분석

### 페이지 구조
```
히어로 (아바타 + 이름 + 이메일 + 뱃지)
└─ 카드 1 (단일 카드에 3가지 기능 혼재)
   ├─ 내 정보 수정 (닉네임)
   ├─ 연동 계정 (Kakao, Naver, Google)
   ├─ alert 성공 메시지
   └─ 회원탈퇴
└─ 카드 2
   └─ 내 메모
```

### 주요 문제점
1. 하나의 카드에 성격이 다른 기능(수정/관리/위험액션)이 혼재
2. 로딩 중 h1 타이틀에 "계정을 불러오는 중입니다" 텍스트 직접 표시
3. 미연동 OAuth 카드도 이메일/닉네임/연동일 필드를 "-"로 표시 (시각적 노이즈)
4. 알림 방식 혼재: OAuth 해제 → alert 박스, 닉네임 저장 → toast
5. 닉네임 글자수 제한 안내 없음, 변경 없어도 저장 버튼 활성화
6. 가입일, 메모 개수 등 사용자 컨텍스트 정보 부재
7. 모달 열릴 때 배경 스크롤 잠금 없음

### API 제약사항
- `AuthMeResponse`에 `createdAt` 필드 없음 → **추가 필요** (타입: `Instant`, `MemberOAuthAccountResponse` 패턴과 통일)
- 메모 API는 Slice 기반, `totalCount` 없음 → **page=0일 때 count 추가 필요**
  - 기존 `BibleSearchSliceResponse`에 `totalCount: Long?` 패턴이 이미 있음 (참고)

---

## 개선 후 목표 구조

```
히어로 (아바타 + 이름 + 이메일 + 뱃지 + 가입일 + 활동 요약)
├─ 카드 1: 내 정보 수정 (닉네임 + 글자수 카운터)
├─ 카드 2: 연동 계정 (연동/미연동 구분 UI)
├─ 카드 3: 내 메모 (개수 표시 + 빈 상태 개선)
└─ 카드 4: Danger Zone - 회원탈퇴 (시각적 분리)
```

---

## 수정 대상 파일

### Backend (Kotlin) - 4개
| 파일 | 변경 내용 |
|------|----------|
| `auth/.../AuthMeResponse.kt` | `createdAt: Instant` 필드 추가 |
| `bible/.../BibleMemoResult.kt` | `MemoSlice`에 `totalCount: Long?` 필드 추가 |
| `bible/.../BibleMemoRepository.kt` | `countByMemberUid()` 메서드 추가 |
| `bible/.../BibleMemoService.kt` | `pageable.pageNumber == 0`일 때 totalCount 조회 로직 추가 |

> **참고:** `BibleMyMemoApi.kt` 컨트롤러는 `BibleMemoResult.MemoSlice`를 그대로 반환(`ResponseEntity.ok(result)`)하므로,
> `MemoSlice` data class에 `totalCount` 필드를 추가하면 JSON 응답에 자동 포함됨. 컨트롤러 수정 불필요.

### Frontend - 3개
| 파일 | 변경 내용 |
|------|----------|
| `templates/member/mypage.html` | 카드 분리, skeleton UI, 구조 재배치, alert 영역 제거 |
| `static/css/member/mypage.css` | skeleton 스타일, 컴팩트 OAuth, danger zone, dead CSS 정리 |
| `static/js/member/mypage.js` | skeleton 제어, 메모 count, 닉네임 UX, 피드백 통일, successMessage 정리 |

---

## 상세 구현 계획

### Phase 1: Backend API 수정

#### 1-1. AuthMeResponse에 createdAt 추가

**파일:** `src/main/kotlin/com/elseeker/auth/adapter/input/api/client/response/AuthMeResponse.kt`

`MemberOAuthAccountResponse`가 `val createdAt: Instant`을 사용하므로 동일 패턴으로 통일.

```kotlin
import java.time.Instant

data class AuthMeResponse(
    val memberUid: String,
    val email: String,
    val role: String,
    val nickname: String,
    val profileImageUrl: String?,
    val provider: String,
    val createdAt: Instant,        // 추가 (Instant 타입, Jackson이 ISO-8601로 직렬화)
) {
    companion object {
        fun from(member: Member): AuthMeResponse {
            val providerRegistrationId = member.oauthAccounts.firstOrNull()
                ?.provider
                ?.registrationId
                ?: ""
            return AuthMeResponse(
                memberUid = member.uid.toString(),
                email = member.email,
                role = member.memberRole.name,
                nickname = member.nickname,
                profileImageUrl = member.profileImageUrl,
                provider = providerRegistrationId,
                createdAt = member.createdAt,  // BaseTimeEntity에서 상속, Lazy 이슈 없음
            )
        }
    }
}
```

> **안전성:** `member.createdAt`은 `BaseTimeEntity`에서 상속된 일반 필드이므로
> LAZY 프록시 이슈 없이 어디서든 안전하게 접근 가능.

#### 1-2. 메모 totalCount 지원

**파일:** `src/main/kotlin/com/elseeker/bible/domain/result/BibleMemoResult.kt`

```kotlin
data class MemoSlice(
    val content: List<MemoItem>,
    val hasNext: Boolean,
    val size: Int,
    val number: Int,
    val totalCount: Long?   // 추가 (page=0일 때만 값 존재, 이후 null)
)
```

**파일:** `src/main/kotlin/com/elseeker/bible/adapter/output/jpa/BibleMemoRepository.kt`

```kotlin
fun countByMemberUid(memberUid: UUID): Long   // 추가
```

> **Spring Data JPA 프로퍼티 경로:** `BibleVerseMemo.member` → `Member.uid` 경로로 탐색.
> 동일 패턴인 `findAllByMemberUid`가 이미 동작 중이므로 문제 없음.

**파일:** `src/main/kotlin/com/elseeker/bible/application/service/BibleMemoService.kt`

`BibleReader.searchBibleVersesSlice()`의 기존 패턴을 따름:

```kotlin
@Transactional(readOnly = true)
fun getMyMemos(memberUid: UUID, pageable: Pageable): BibleMemoResult.MemoSlice {
    val sortedPageable = if (pageable.sort.isUnsorted) {
        PageRequest.of(
            pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "updatedAt")
        )
    } else {
        pageable
    }
    val slice = bibleMemoRepository.findAllByMemberUid(memberUid, sortedPageable)

    val bookNameMap = resolveBookNames(slice.content)

    // 기존 BibleReader.searchBibleVersesSlice() 패턴:
    // page=0일 때만 count 쿼리 실행, 이후 페이지는 null
    val totalCount = if (sortedPageable.pageNumber == 0) {
        bibleMemoRepository.countByMemberUid(memberUid)
    } else {
        null
    }

    return BibleMemoResult.MemoSlice(
        content = slice.content.map { memo ->
            val bookName = bookNameMap[memo.translationId to memo.bookOrder] ?: ""
            BibleMemoResult.MemoItem.from(memo, bookName)
        },
        hasNext = slice.hasNext(),
        size = slice.size,
        number = slice.number,
        totalCount = totalCount
    )
}
```

---

### Phase 2: HTML 구조 재배치

**파일:** `src/main/resources/templates/member/mypage.html`

#### 2-1. 히어로 섹션 개선

```html
<section class="page-hero mypage-hero" aria-labelledby="mypageTitle">
    <div class="page-hero-content mypage-hero-content">
        <div class="mypage-avatar">
            <img id="mypageAvatar" src="/images/user.png" alt="프로필 이미지" loading="lazy">
        </div>
        <div class="mypage-intro">
            <!-- skeleton 상태 (로딩 중) -->
            <div id="mypageSkeleton" class="mypage-skeleton-group">
                <div class="skeleton skeleton-title"></div>
                <div class="skeleton skeleton-text"></div>
                <div class="skeleton skeleton-badges"></div>
            </div>
            <!-- 실제 콘텐츠 (로딩 완료 후 표시) -->
            <div id="mypageProfile" class="d-none">
                <h1 id="mypageTitle" class="mypage-title"></h1>
                <p id="mypageEmail" class="mypage-email"></p>
                <div class="mypage-badges">
                    <span id="mypageRole" class="badge mypage-badge role">회원</span>
                    <span id="mypageProvider" class="badge mypage-badge provider"></span>
                    <span id="mypageJoinDate" class="badge mypage-badge join-date"></span>
                </div>
            </div>
        </div>
    </div>
    <!-- 활동 요약 -->
    <div id="mypageStats" class="mypage-stats d-none">
        <div class="mypage-stat-item">
            <span class="mypage-stat-value" id="mypageMemoCount">0</span>
            <span class="mypage-stat-label">메모</span>
        </div>
        <div class="mypage-stat-item">
            <span class="mypage-stat-value" id="mypageOAuthCount">0</span>
            <span class="mypage-stat-label">연동 계정</span>
        </div>
    </div>
</section>
```

#### 2-2. 카드 분리 구조

현재 하나의 `<article>` 안에 있는 내용을 4개 독립 카드로 분리.
기존 CSS 클래스명 `mypage-info-card`를 유지하여 기존 스타일(`animation-delay`, `border`, `shadow`) 호환.

```html
<!-- 카드 1: 내 정보 수정 -->
<div class="row g-3">
    <div class="col-12">
        <article class="card card-panel card-soft mypage-info-card h-100 border-0 shadow-sm">
            <div class="card-body p-4">
                <h3 class="h6 fw-bold mb-3">내 정보 수정</h3>
                <form id="mypageEditForm" class="mypage-edit-form">
                    <div class="mb-3">
                        <label class="form-label" for="mypageNicknameInput">닉네임</label>
                        <div class="mypage-nickname-row">
                            <input id="mypageNicknameInput" class="form-control" type="text"
                                   autocomplete="nickname" required maxlength="50"
                                   placeholder="닉네임을 입력해 주세요">
                            <button id="mypageSaveButton" class="btn btn-primary" type="submit"
                                    disabled>저장하기</button>
                        </div>
                        <div class="mypage-nickname-counter">
                            <span id="mypageNicknameCount">0</span>/50
                        </div>
                    </div>
                </form>
            </div>
        </article>
    </div>
</div>

<!-- 카드 2: 연동 계정 -->
<div class="row g-3 mt-1">
    <div class="col-12">
        <article class="card card-panel card-soft mypage-info-card h-100 border-0 shadow-sm">
            <div class="card-body p-4">
                <h3 class="h6 fw-bold mb-3">연동 계정</h3>
                <div id="mypageOAuthAccountsList" class="mypage-oauth-list">
                    <!-- 기존 3개 OAuth 카드 (Kakao, Naver, Google) 유지 -->
                    <!-- is-empty 클래스 → CSS에서 compact UI 적용 -->
                </div>
            </div>
        </article>
    </div>
</div>

<!-- 카드 3: 내 메모 -->
<div class="row g-3 mt-1">
    <div class="col-12">
        <article class="card card-panel card-soft mypage-info-card h-100 border-0 shadow-sm">
            <div class="card-body p-4">
                <div class="mypage-memo-header">
                    <h3 class="h6 fw-bold mb-0">내 메모</h3>
                    <span id="mypageMemoCountBadge" class="badge mypage-badge-count d-none"></span>
                </div>
                <!-- 메모 skeleton -->
                <div id="mypageMemoSkeleton" class="mypage-skeleton-group">
                    <div class="skeleton skeleton-memo-card"></div>
                    <div class="skeleton skeleton-memo-card"></div>
                </div>
                <!-- 메모 목록 -->
                <div id="mypageMemoList" class="mypage-memo-list"></div>
                <div id="mypageMemoEmpty" class="mypage-memo-empty d-none">
                    <p class="mb-2">아직 작성한 메모가 없습니다.</p>
                    <a href="/web/bible/translation" class="btn btn-outline-primary btn-sm">
                        성경 읽기에서 메모를 남겨보세요
                    </a>
                </div>
                <button id="mypageMemoMore" class="btn btn-outline-secondary w-100 mt-3 d-none"
                        type="button">더보기</button>
            </div>
        </article>
    </div>
</div>

<!-- 카드 4: Danger Zone (최하단 분리) -->
<div class="row g-3 mt-1">
    <div class="col-12">
        <article class="mypage-danger-zone">
            <div class="mypage-danger-zone-body">
                <div>
                    <h3 class="mypage-danger-zone-title">Danger Zone</h3>
                    <p class="mypage-danger-zone-desc">
                        탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.
                    </p>
                </div>
                <a class="btn btn-outline-danger" href="/web/member/withdraw">회원탈퇴</a>
            </div>
        </article>
    </div>
</div>
```

#### 2-3. 삭제 대상 HTML

```html
<!-- 삭제: alert 메시지 영역 (toast로 통일) -->
<div id="mypageSuccessMessage" class="alert alert-success mt-3 d-none" role="alert"></div>

<!-- 삭제: 기존 withdraw 영역 (Danger Zone 카드로 이동) -->
<div class="mypage-withdraw-card">...</div>
```

---

### Phase 3: CSS 수정

**파일:** `src/main/resources/static/css/member/mypage.css`

#### 3-1. Skeleton UI 스타일 추가

```css
/* ── Skeleton UI ── */
.mypage-skeleton-group {
    padding: 0.25rem 0;
}

.skeleton {
    background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s ease-in-out infinite;
    border-radius: 0.5rem;
}

.skeleton-title { width: 60%; height: 2rem; margin-bottom: 0.5rem; }
.skeleton-text { width: 40%; height: 1rem; margin-bottom: 1rem; }
.skeleton-badges { width: 50%; height: 1.5rem; }
.skeleton-memo-card { width: 100%; height: 5rem; margin-bottom: 0.75rem; }

@keyframes shimmer {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}
```

#### 3-2. OAuth 미연동 compact 스타일

기존 `.mypage-oauth-card.is-empty .mypage-oauth-details`의 `opacity: 0.4`를 `display: none`으로 변경.

```css
/* 미연동 카드: compact */
.mypage-oauth-card.is-empty {
    padding: 0.75rem 1rem;
}
.mypage-oauth-card.is-empty .mypage-oauth-details {
    display: none;  /* 기존: opacity: 0.4 → 완전 숨김 */
}
.mypage-oauth-card.is-empty .mypage-oauth-header {
    margin-bottom: 0;
}
.mypage-oauth-card.is-empty .mypage-oauth-footer {
    flex-direction: row;
    align-items: center;
}
```

#### 3-3. 연동 상태 배지

```css
.mypage-oauth-status-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.3rem;
    font-size: 0.75rem;
    font-weight: 600;
    padding: 0.15rem 0.5rem;
    border-radius: 999px;
}
.mypage-oauth-status-badge.is-linked {
    background: #f0fdf4;
    color: #166534;
    border: 1px solid #dcfce7;
}
.mypage-oauth-status-badge.is-unlinked {
    background: #f8fafc;
    color: #94a3b8;
    border: 1px solid #e2e8f0;
}
```

#### 3-4. Danger Zone 스타일

```css
.mypage-danger-zone {
    margin-top: 2rem;
    border: 1px solid #fecaca;
    border-radius: 0.75rem;
    background: #fef2f2;
}
.mypage-danger-zone-body {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    padding: 1.25rem;
}
.mypage-danger-zone-title {
    font-size: 0.95rem;
    font-weight: 600;
    color: #991b1b;
    margin: 0 0 0.25rem;
}
.mypage-danger-zone-desc {
    font-size: 0.8rem;
    color: #b91c1c;
    margin: 0;
}
```

#### 3-5. 활동 요약 스탯

```css
.mypage-stats {
    display: flex;
    gap: 2rem;
    margin-top: 1.5rem;
    padding-top: 1.5rem;
    border-top: 1px solid #e2e8f0;
}
.mypage-stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
}
.mypage-stat-value {
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--mypage-deep);
}
.mypage-stat-label {
    font-size: 0.75rem;
    color: var(--mypage-muted);
}
```

#### 3-6. 닉네임 카운터

```css
.mypage-nickname-counter {
    text-align: right;
    font-size: 0.75rem;
    color: #94a3b8;
    margin-top: 0.35rem;
}
```

#### 3-7. 가입일 뱃지

```css
.mypage-badge.join-date {
    background: #f8fafc;
    color: #64748b;
    border: 1px solid #e2e8f0;
}
```

#### 3-8. 메모 헤더

```css
.mypage-memo-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 1rem;
}
.mypage-badge-count {
    background: #eff6ff;
    color: #2563eb;
    font-size: 0.75rem;
    font-weight: 600;
    padding: 0.2rem 0.6rem;
    border-radius: 999px;
}
```

#### 3-9. 모바일 개선

기존 576px 미디어쿼리에서 OAuth 필드의 `grid-template-columns: 1fr` (세로 스택)을
`60px 1fr`로 변경하여 라벨-값 인라인 유지.

```css
@media (max-width: 576px) {
    /* OAuth 필드: 60px 라벨 유지 (기존 1fr 세로 스택 → 인라인 유지) */
    .mypage-oauth-field {
        grid-template-columns: 60px 1fr;
        gap: 0.5rem;
    }
    .mypage-oauth-field span {
        font-size: 0.78rem;
        text-transform: none;
        letter-spacing: normal;
    }

    /* Danger Zone 세로 배치 */
    .mypage-danger-zone-body {
        flex-direction: column;
        align-items: flex-start;
    }
    .mypage-danger-zone-body .btn {
        width: 100%;
    }

    /* 활동 스탯 중앙 정렬 */
    .mypage-stats {
        justify-content: center;
    }
}
```

#### 3-10. Dead CSS 정리

HTML에서 더 이상 사용하지 않는 CSS 규칙 제거:

**회원탈퇴 관련 (Danger Zone으로 교체):**
- `.mypage-withdraw-card` (L365-374)
- `.mypage-withdraw-title` (L376-381)
- `.mypage-withdraw-desc` (L383-387)
- `.mypage-withdraw-btn` (L389-392)

**기존부터 미사용 (HTML에서 참조하지 않는 클래스):**
- `.mypage-eyebrow` (L68-74)
- `.mypage-status-pill` (L275-286)
- `.mypage-divider` (L288-292)
- `.mypage-action-grid` 및 하위 규칙 (L337-363)
- `.mypage-tips` 및 하위 규칙 (L394-405)
- `.mypage-daily-verse-text` (L407-412)
- `.mypage-daily-verse-ref` (L414-419)

---

### Phase 4: JavaScript 수정

**파일:** `src/main/resources/static/js/member/mypage.js`

#### 4-1. Skeleton 제어

```javascript
// 로딩 완료 시 skeleton 숨기고 실제 프로필 표시
const showProfile = () => {
    document.getElementById("mypageSkeleton")?.classList.add("d-none");
    document.getElementById("mypageProfile")?.classList.remove("d-none");
    document.getElementById("mypageStats")?.classList.remove("d-none");
};

// onAuthenticated 콜백에서 프로필 데이터 설정 후 showProfile() 호출
```

#### 4-2. 가입일 표시

```javascript
// onAuthenticated 내부
const joinDateBadge = document.getElementById("mypageJoinDate");
if (data.createdAt) {
    const joinDate = new Date(data.createdAt);
    const formatted = joinDate.toLocaleDateString("ko-KR", {
        year: "numeric", month: "long"
    });
    updateText(joinDateBadge, `가입 ${formatted}`);
}
```

#### 4-3. 닉네임 UX 개선

닉네임 제한은 `Member.kt`의 `@Column(length = 50)`에 따라 **50자**.

```javascript
const nicknameCount = document.getElementById("mypageNicknameCount");

// 글자수 카운터 + 저장 버튼 활성화 제어
nicknameInput.addEventListener("input", () => {
    const len = nicknameInput.value.trim().length;
    if (nicknameCount) {
        nicknameCount.textContent = len;
    }
    // 초기값과 동일하면 저장 버튼 비활성화
    saveButton.disabled = (nicknameInput.value.trim() === initialNickname);
});

// 초기 로드 시 버튼 비활성화 (기존 setFormEnabled(false)와 함께)
saveButton.disabled = true;

// 프로필 로드 완료 후 카운터 초기화
nicknameCount.textContent = initialNickname.length;
```

#### 4-4. 알림 피드백 통일 — successMessage 완전 제거

`successMessage` 관련 코드 **3곳** 모두 정리:

```javascript
// 1. 변수 선언 제거 (기존 L35)
//    const successMessage = document.getElementById("mypageSuccessMessage");  // 삭제

// 2. resetMessages() 함수 정리 (기존 L99-112)
//    successMessage 참조 블록 제거. 남은 역할은 toast 초기화뿐이므로
//    resetMessages()는 hideSaveToast()로 대체하거나 함수 내부를 정리.
const resetMessages = () => {
    hideSaveToast();
};

// 3. OAuth 해제 성공 시 (기존 L361-364)
//    기존: successMessage.textContent = "연동 계정이 해제되었습니다.";
//          successMessage.classList.remove("d-none");
//    변경: toast로 통일
showSaveToast("연동 계정이 해제되었습니다.");
```

#### 4-5. 메모 개수 표시

```javascript
// loadMyMemos에서 page=0 응답의 totalCount 활용
if (!append && data.totalCount != null) {
    const countBadge = document.getElementById("mypageMemoCountBadge");
    if (countBadge) {
        countBadge.textContent = data.totalCount;
        countBadge.classList.remove("d-none");
    }
    // 히어로 스탯 업데이트
    updateText(document.getElementById("mypageMemoCount"), data.totalCount);
}
```

#### 4-6. 메모 skeleton 제어

```javascript
const memoSkeleton = document.getElementById("mypageMemoSkeleton");

// loadMyMemos 시작 시 (첫 페이지만)
if (!append) {
    memoSkeleton?.classList.remove("d-none");
}

// loadMyMemos 완료 시 (finally 블록)
memoSkeleton?.classList.add("d-none");
```

#### 4-7. OAuth 카드 compact 처리 + 상태 배지

`updateOAuthCards` 함수 내부에서 `.mypage-oauth-status` 요소의 클래스를 배지 형태로 변경:

```javascript
// 연동 상태 배지 업데이트
if (status) {
    status.className = linkedAccount
        ? "mypage-oauth-status-badge is-linked"
        : "mypage-oauth-status-badge is-unlinked";
    status.textContent = linkedAccount ? "연동됨" : "미연동";
}
```

> **참고:** 미연동 카드의 `is-empty` 클래스는 기존 로직이 이미 처리 중.
> CSS에서 `.mypage-oauth-card.is-empty .mypage-oauth-details { display: none }` 추가로
> compact UI가 자동 적용됨. JS 추가 수정 불필요.

#### 4-8. OAuth 카드 aria-label 추가

```javascript
// updateOAuthCards 내부
card.setAttribute("aria-label",
    linkedAccount
        ? `${providerLabel} 계정 연동됨`
        : `${providerLabel} 계정 미연동`
);
```

#### 4-9. 히어로 OAuth 카운트 업데이트

```javascript
// renderOAuthAccounts 내부
updateText(document.getElementById("mypageOAuthCount"), providerMap.size);
```

#### 4-10. toast 표시 시간 조정

```javascript
// 기존: 2500ms → 변경: 4000ms (접근성: 스크린 리더 인지 시간 확보)
saveToastTimer = setTimeout(() => {
    hideSaveToast();
}, 4000);
```

#### 4-11. 모달 배경 스크롤 잠금

```javascript
const openConfirmModal = (providerLabel) => {
    // ...기존 코드
    document.body.style.overflow = "hidden";  // 추가: 배경 스크롤 잠금
};

const closeConfirmModal = () => {
    // ...기존 코드
    document.body.style.overflow = "";  // 추가: 스크롤 복원
};
```

> **경로 확인 완료:** ESC 키(L519-523), backdrop 클릭(L513-518), confirmOAuthUnlink finally(L368-372)
> 모두 `closeConfirmModal()`을 호출하므로 모든 닫기 경로에서 스크롤이 복원됨.

#### 4-12. OAuth 섹션 로딩 상태

OAuth 카드는 구조가 복잡하고 3개 카드가 이미 HTML에 정적으로 존재하므로,
skeleton 대신 기존 "연동 확인 중" 텍스트 방식을 유지한다.
이유: OAuth 카드는 항상 3개가 렌더링되어 있고, 로딩 중에도 레이아웃이 유지됨.
skeleton으로 교체하면 카드 구조 자체를 JS로 동적 생성해야 하여 복잡도가 과도하게 증가함.

---

### Phase 5: 버전 캐시 무효화

mypage.html에서 CSS/JS 참조 버전 업데이트:
- `mypage.css?v=2.3` → `mypage.css?v=3.0`
- `mypage.js?v=2.3` → `mypage.js?v=3.0`

---

## 구현 순서

1. **Backend**: `AuthMeResponse`에 `createdAt: Instant` 추가
2. **Backend**: `BibleMemoResult.MemoSlice`에 `totalCount: Long?` 추가 + Repository/Service 수정
3. **HTML**: `mypage.html` 카드 분리 + skeleton + 구조 재배치 + alert 제거
4. **CSS**: `mypage.css` 스타일 추가 + dead CSS 정리
5. **JS**: `mypage.js` 로직 수정 (skeleton, 닉네임 UX, 피드백 통일, 메모 count, 접근성)
6. **버전 업데이트**: HTML 내 CSS/JS 쿼리 파라미터 버전 갱신
7. **빌드/테스트**: `./gradlew build`로 Backend 변경 검증

## 주의사항

- 기존 기능 로직(OAuth 연동/해제, 닉네임 저장, 메모 페이지네이션)은 그대로 유지
- `successMessage` 관련 HTML/JS 코드 3곳(변수 선언, resetMessages, OAuth 해제 성공) 모두 정리
- `resetMessages()` 함수는 successMessage 제거 후 `hideSaveToast()` 호출만 남으므로, 함수 내부를 정리하거나 호출부를 `hideSaveToast()`로 직접 교체
- HTML 카드 분리 시 기존 CSS 클래스명 `mypage-info-card` 유지 (기존 스타일 호환)
- 닉네임 글자수 제한은 `Member.kt`의 `@Column(length = 50)` 기준으로 **50자**
- Kotlin 코드 변경이 포함되므로 `./gradlew build` 실행 필요
- CSS/JS 파일 수정 후 HTML 내 `?v=` 버전 반드시 갱신
