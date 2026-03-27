# 마이페이지 UI/UX 개선 설계 문서

## 구현 완료 상태

모든 Phase 구현 완료. 이 문서는 최종 구현 결과를 정리한 설계 문서이다.

---

## 페이지 구조

```
히어로 (아바타 + 이름 + 이메일 + 뱃지[역할, 연동 계정 수, 가입일])
├─ 탭 네비게이션 (sticky, 스크롤 시 top-nav 연동)
│   ├─ [계정 설정] 탭
│   │   ├─ 카드 1: 내 정보 수정 (닉네임 + 글자수 카운터 + 변경 감지 저장 버튼)
│   │   ├─ 카드 2: 연동 계정 (연동/미연동 상태 배지, compact UI)
│   │   └─ Danger Zone: 회원탈퇴 (시각적 분리)
│   └─ [내 메모] 탭 (메모 개수 배지)
│       └─ 카드: 내 메모 (필터 + 목록 + skeleton + 빈 상태 + 더보기)
├─ Toast 알림 (success/error/info 통일)
└─ OAuth 연동 해제 확인 모달 (포커스 트랩 + 배경 스크롤 잠금)
```

### 이전 구조 대비 변경점

| 항목 | 이전 | 현재 |
|------|------|------|
| 레이아웃 | 단일 스크롤 4카드 순차 배치 | 2탭 분리 (계정 설정 / 내 메모) |
| 로딩 UI | h1에 "계정을 불러오는 중입니다" 텍스트 | Skeleton UI (프로필 + 메모) |
| 미연동 OAuth | 이메일/닉네임/연동일 "-" 표시 | compact 카드 (details 숨김) |
| 연동 상태 | 텍스트 | 배지 (연동됨/미연동, 색상 구분) |
| 알림 | alert 박스 + toast 혼재 | toast 통일 (success/error/info) |
| 닉네임 저장 | 변경 없어도 저장 가능 | 변경 감지 + 글자수 카운터 (50자) |
| 메모 목록 | 단순 페이지네이션 | 번역본/성경 필터 + 탭 배지 카운트 |
| 모달 | 배경 스크롤 잠금 없음 | overflow hidden + 포커스 트랩 |
| 가입일 | 미표시 | 히어로 배지에 "가입 YYYY년 M월" 표시 |

---

## 수정 파일 목록

### Backend (Kotlin) — 4개

| 파일 | 변경 내용 |
|------|----------|
| `auth/.../AuthMeResponse.kt` | `createdAt: Instant` 필드 추가 |
| `bible/.../BibleMemoResult.kt` | `MemoSlice`에 `totalCount: Long?` 필드 추가 |
| `bible/.../BibleMemoRepository.kt` | `countByMemberUid()` 및 필터별 count 메서드 추가 |
| `bible/.../BibleMemoService.kt` | 필터(translationId, bookOrder) 조합별 count 분기 + 메모 번역본/성경 목록 API 추가 |

### Frontend — 3개

| 파일 | 변경 내용 |
|------|----------|
| `templates/member/mypage.html` | 탭 구조, skeleton UI, 카드 분리, 메모 필터, toast, 모달 |
| `static/css/member/mypage.css` | 탭/skeleton/compact OAuth/status badge/danger zone/필터/반응형 |
| `static/js/member/mypage.js` | 탭 전환, skeleton 제어, 닉네임 UX, 메모 필터/count, 모달 포커스 트랩 |

---

## 상세 구현 내역

### 1. Backend API

#### 1-1. AuthMeResponse에 createdAt 추가

**파일:** `src/main/kotlin/com/elseeker/auth/adapter/input/api/client/response/AuthMeResponse.kt`

`MemberOAuthAccountResponse`가 `val createdAt: Instant`을 사용하므로 동일 패턴으로 통일.
`member.createdAt`은 `BaseTimeEntity`에서 상속된 일반 필드이므로 LAZY 프록시 이슈 없이 안전.

```kotlin
data class AuthMeResponse(
    val memberUid: String,
    val email: String,
    val role: String,
    val nickname: String,
    val profileImageUrl: String?,
    val provider: String,
    val createdAt: Instant,
)
```

#### 1-2. 메모 totalCount + 필터별 count 지원

**파일:** `src/main/kotlin/com/elseeker/bible/domain/result/BibleMemoResult.kt`

```kotlin
data class MemoSlice(
    val content: List<MemoItem>,
    val hasNext: Boolean,
    val size: Int,
    val number: Int,
    val totalCount: Long?   // page=0일 때만 값 존재, 이후 null
)
```

**파일:** `src/main/kotlin/com/elseeker/bible/adapter/output/jpa/BibleMemoRepository.kt`

필터 조합별 count 메서드:

```kotlin
fun countByMemberUid(memberUid: UUID): Long
fun countByMemberUidAndBookOrder(memberUid: UUID, bookOrder: Int): Long
fun countByMemberUidAndTranslationId(memberUid: UUID, translationId: Long): Long
fun countByMemberUidAndTranslationIdAndBookOrder(memberUid: UUID, translationId: Long, bookOrder: Int): Long
```

메모 필터용 조회 메서드:

```kotlin
fun findDistinctTranslationIdsByMemberUid(memberUid: UUID): List<Long>
fun findDistinctBookOrdersByMemberUidAndTranslationId(memberUid: UUID, translationId: Long): List<Int>
fun findAllByMemberUidAndTranslationId(memberUid: UUID, translationId: Long, pageable: Pageable): Slice<BibleVerseMemo>
fun findAllByMemberUidAndTranslationIdAndBookOrder(memberUid: UUID, translationId: Long, bookOrder: Int, pageable: Pageable): Slice<BibleVerseMemo>
```

**파일:** `src/main/kotlin/com/elseeker/bible/application/service/BibleMemoService.kt`

`getMyMemos()`에서 `translationId`, `bookOrder` 필터 조합에 따라 조회/count 분기:

```kotlin
fun getMyMemos(
    memberUid: UUID,
    pageable: Pageable,
    translationId: Long? = null,
    bookOrder: Int? = null
): BibleMemoResult.MemoSlice
```

추가 서비스 메서드:
- `getMemoTranslations(memberUid)` — 메모가 존재하는 번역본 목록
- `getMemoBookList(memberUid, translationId)` — 특정 번역본 내 메모가 존재하는 성경 목록

> **API 참고:** `BibleMyMemoApi.kt` 컨트롤러는 `BibleMemoResult.MemoSlice`를 그대로 반환하므로,
> `MemoSlice`에 `totalCount` 필드를 추가하면 JSON 응답에 자동 포함됨. 컨트롤러 수정 불필요.

---

### 2. HTML 구조

**파일:** `src/main/resources/templates/member/mypage.html`

#### 2-1. 히어로 섹션 (Skeleton + 프로필)

```html
<section class="page-hero mypage-hero" aria-labelledby="mypageTitle">
    <div class="page-hero-content mypage-hero-content">
        <div class="mypage-avatar">
            <img id="mypageAvatar" src="/images/user.png" alt="프로필 이미지" loading="lazy">
        </div>
        <div class="mypage-intro">
            <!-- skeleton (로딩 중) -->
            <div id="mypageSkeleton" class="mypage-skeleton-group">...</div>
            <!-- 실제 프로필 (로딩 후 표시) -->
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
</section>
```

#### 2-2. 탭 네비게이션

sticky 탭으로 "계정 설정" / "내 메모" 분리. URL 쿼리 파라미터(`?tab=settings|memo`)로 탭 상태 유지.
내 메모 탭에 `mypageTabMemoBadge`로 메모 개수 배지 표시.

```html
<div class="mypage-tabs-wrapper">
    <nav class="mypage-tabs" role="tablist" aria-label="마이페이지 탭">
        <button class="mypage-tab active" data-tab="settings" ...>계정 설정</button>
        <button class="mypage-tab" data-tab="memo" ...>
            내 메모 <span id="mypageTabMemoBadge" class="mypage-tab-badge d-none"></span>
        </button>
    </nav>
</div>
```

#### 2-3. 계정 설정 탭

3개 섹션으로 구성:

1. **내 정보 수정** — 닉네임 input + 글자수 카운터(`/50`) + 저장 버튼(변경 시에만 활성화)
2. **연동 계정** — Kakao/Naver/Google 3개 OAuth 카드 (연동/미연동 상태에 따라 compact UI)
3. **Danger Zone** — 회원탈퇴 링크 (빨간 테두리, 최하단 분리)

#### 2-4. 내 메모 탭

- 메모 헤더 (제목 + 개수 배지)
- 필터 (번역본 select + 성경 select, 메모 존재 시에만 표시)
- Skeleton → 메모 목록 or 빈 상태 → 더보기 버튼

#### 2-5. 삭제된 HTML

- `#mypageSuccessMessage` alert 영역 (toast로 통일)
- `.mypage-withdraw-card` (Danger Zone으로 교체)
- `#mypageStats` 활동 요약 (탭 배지로 대체)

---

### 3. CSS

**파일:** `src/main/resources/static/css/member/mypage.css`

#### 주요 스타일 블록

| 블록 | 설명 |
|------|------|
| `.mypage-tabs-wrapper` / `.mypage-tab` | sticky 탭 (top-nav 연동, `body.top-nav-hidden` 시 `top: 0`) |
| `.mypage-tab-badge` | 탭 내 메모 개수 배지 (active/inactive 색상 구분) |
| `.mypage-tab-panel` | 탭 전환 시 `tabFadeIn` 애니메이션 |
| `.skeleton` / `@keyframes shimmer` | Skeleton UI 애니메이션 |
| `.mypage-oauth-status-badge` | 연동/미연동 상태 배지 (`.is-linked` 초록, `.is-unlinked` 회색) |
| `.mypage-oauth-card.is-empty` | 미연동 카드 compact (details 숨김, 패딩 축소) |
| `.mypage-danger-zone` | 빨간 테두리/배경, flex 레이아웃 |
| `.mypage-nickname-counter` | 글자수 카운터 (우측 정렬, 0.75rem) |
| `.mypage-badge.join-date` | 가입일 배지 (회색 계열) |
| `.mypage-memo-filter` | 필터 select 가로 배치 (모바일 576px 이하 세로) |
| `.mypage-badge-count` | 메모 개수 배지 (파란 계열) |

#### 반응형

- **768px 이하**: 히어로 그리드 1열 → 세로 중앙 배치
- **576px 이하**: 닉네임 row 세로 배치, OAuth 카드 패딩 축소, 메모 필터 세로 배치, Danger Zone 세로 배치

#### 삭제된 CSS

기존 미사용 클래스 정리 완료:
- `.mypage-withdraw-card` / `.mypage-withdraw-title` / `.mypage-withdraw-desc` / `.mypage-withdraw-btn`
- `.mypage-eyebrow`, `.mypage-status-pill`, `.mypage-divider`
- `.mypage-action-grid`, `.mypage-tips`, `.mypage-daily-verse-text`, `.mypage-daily-verse-ref`

---

### 4. JavaScript

**파일:** `src/main/resources/static/js/member/mypage.js`

#### 4-1. 탭 시스템

```javascript
const switchTab = (tabName) => { ... };
```

- 탭 버튼 active 토글 + `aria-selected` 제어
- 탭 패널 `d-none` 토글
- URL `?tab=` 쿼리 파라미터로 상태 유지 (`replaceState`)
- 탭별 스크롤 위치 기억/복원

#### 4-2. Skeleton 제어

```javascript
const showProfile = () => {
    mypageSkeleton?.classList.add("d-none");
    mypageProfile?.classList.remove("d-none");
};
```

메모 skeleton도 `loadMyMemos` 시작/완료 시 토글.

#### 4-3. 가입일 표시

`onAuthenticated` 콜백에서 `data.createdAt`을 `ko-KR` 로케일로 "가입 YYYY년 M월" 형태로 표시.

#### 4-4. 닉네임 UX

- `input` 이벤트로 글자수 카운터 실시간 갱신
- 초기값(`initialNickname`)과 동일하면 저장 버튼 `disabled`
- `setFormEnabled(true)` 호출 후 `saveButton.disabled = true` 명시적 설정 (2곳: 초기 로드, 저장 성공 후)

#### 4-5. 알림 통일 (toast)

`showSaveToast(message, variant)` — variant: `"success"` (기본), `"error"`, `"info"`
- OAuth 해제 성공 → `showSaveToast("연동 계정이 해제되었습니다.")`
- `successMessage` alert 관련 코드 완전 제거

Toast 표시 시간: **4000ms** (접근성: 스크린 리더 인지 시간 확보)

#### 4-6. 메모 개수 + 탭 배지

`loadMyMemos`에서 `page=0` 응답의 `totalCount` 활용:
- `#mypageMemoCountBadge` — 메모 카드 헤더 내 개수 표시
- `#mypageTabMemoBadge` — 탭 버튼 내 개수 배지 (0이면 숨김)

#### 4-7. 메모 필터

- `loadMemoTranslationFilter()` — 메모가 존재하는 번역본 목록 로드, 1개면 자동 선택
- `loadMemoBookFilter(translationId)` — 선택된 번역본의 성경 목록 로드
- 필터 변경 시 `memoPage = 0`으로 리셋 후 재조회

#### 4-8. OAuth 카드

- `updateOAuthCards(providerMap)` — 연동/미연동에 따라:
  - `is-empty` 클래스 토글 (CSS에서 compact UI 자동 적용)
  - 상태 배지 클래스 설정 (`mypage-oauth-status-badge is-linked|is-unlinked`)
  - `aria-label` 설정 (`"${provider} 계정 연동됨|미연동"`)
- OAuth 에러 URL 처리: `?oauthError=CODE` 파라미터 감지 → toast 표시 후 URL 정리

#### 4-9. 모달 접근성

- `openConfirmModal()` — `document.body.style.overflow = "hidden"` (배경 스크롤 잠금)
- `closeConfirmModal()` — `overflow` 복원 + 트리거 요소로 포커스 반환
- `trapFocusInModal()` — Tab/Shift+Tab 키로 모달 내 포커스 순환
- 닫기 경로: ESC 키, backdrop 클릭, 취소 버튼, 연동 해제 완료 — 모두 `closeConfirmModal()` 호출

---

### 5. 캐시 버전

| 파일 | 버전 |
|------|------|
| `mypage.css` | `?v=4.1` |
| `mypage.js` | `?v=4.0` |

---

## 주의사항

- 기존 기능 로직(OAuth 연동/해제, 닉네임 저장, 메모 페이지네이션)은 그대로 유지
- 닉네임 글자수 제한은 `Member.kt`의 `@Column(length = 50)` 기준 **50자**
- CSS/JS 파일 수정 시 HTML 내 `?v=` 버전 반드시 갱신
- Kotlin 코드 변경 포함 시 `./gradlew build` 실행 필요
- 프론트엔드만 변경 시 빌드/테스트 불필요
