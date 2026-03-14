# 마이페이지 UI/UX 전문가 검토 리포트

## 1. 검토 개요

**대상 파일:**
- `src/main/resources/templates/member/mypage.html`
- `src/main/resources/static/css/member/mypage.css` (v3.3)
- `src/main/resources/static/js/member/mypage.js` (v3.3)

**검토 기준:** 정보 구조, 시각적 계층, 일관성, 접근성(WCAG 2.1 AA), 모바일 UX, 인터랙션 디자인, 마이크로 인터랙션, 사용자 흐름, 에러 방지, 성능 UX

**검토 일자:** 2026-03-14

---

## 2. 화면 구성

```
마이페이지
  ├─ 히어로 섹션 (아바타 + 닉네임 + 이메일 + 뱃지 + 활동 스탯)
  ├─ 탭 네비게이션 (Segmented Control 패턴)
  │   ├─ [계정 설정] 탭
  │   │   ├─ 내 정보 수정 카드 (닉네임 편집 폼)
  │   │   ├─ 연동 계정 카드 (Kakao / Naver / Google OAuth)
  │   │   └─ Danger Zone (회원탈퇴 링크)
  │   └─ [내 메모] 탭
  │       └─ 성경 구절 메모 목록 + 더보기
  └─ Toast / Modal (오버레이)
```

---

## 3. 발견 이슈 및 수정 현황

| 심각도 | 총 건수 | 수정 완료 | 미수정 |
|--------|---------|----------|--------|
| Critical | 3건 | 3건 | 0건 |
| Major | 15건 | 3건 | 12건 |
| Minor | 6건 | 0건 | 6건 |
| 버그 | 1건 | 1건 | 0건 |

---

## 4. Critical 이슈 — 모두 수정 완료

### C-01. 모달 포커스 트랩 미구현 — ✅ 수정 완료

- **WCAG:** 2.4.3 Focus Order 위반
- **현상:** OAuth 연동 해제 확인 모달에 포커스 트랩 없음. Tab 키로 포커스가 모달 뒤로 이동. 닫힌 후 트리거 버튼으로 포커스 복원 안 됨.
- **수정 내용:**
  - `trapFocusInModal()` 함수 추가 — Tab/Shift+Tab 키 이벤트를 가로채 모달 내 focusable 요소(취소, 연동해제 버튼) 내에서 순환
  - `modalTriggerElement` 변수에 모달 열기 전 포커스 요소 저장, `closeConfirmModal()` 시 복원
  - 모달 open/close 시 `keydown` 리스너 등록/해제

### C-02. 메모 카드 innerHTML XSS 취약 패턴 — ✅ 수정 완료

- **현상:** `createMemoCard()`에서 `memo.bookName` 등을 이스케이프 없이 `innerHTML`에 삽입. `memo.content`는 `<`/`>` 치환만 수행하며 `"`, `'`, `&` 미처리.
- **수정 내용:**
  - `innerHTML` 제거, `document.createElement()` + `textContent`로 DOM 개별 생성
  - URL 파라미터에 `encodeURIComponent()` 적용

### C-03. 메모 로드 실패 시 완전 무음 처리 — ✅ 수정 완료

- **현상:** `loadMyMemos()` catch 블록이 `// silently fail`로 비어있음. 에러와 빈 상태 구분 불가.
- **수정 내용:**
  - catch 블록에 에러 메시지("메모를 불러오지 못했습니다.") + "다시 시도" 버튼 UI 표시
  - 재시도 시 `memoPage = 0`으로 초기화 후 `loadMyMemos(false)` 재호출

---

## 5. Major 이슈

### 접근성 (Accessibility) — 미수정

#### M-01. 스켈레톤 로딩 상태에 접근성 정보 부재

- **WCAG:** 4.1.3 Status Messages
- **현상:** `#mypageSkeleton`, `#mypageMemoSkeleton`에 `role="status"`, `aria-live`, `aria-label="로딩 중"` 없음.
- **수정 방향:** 스켈레톤 컨테이너에 `role="status"` 및 `aria-label="프로필 정보 로딩 중"` 추가

#### M-02. 토스트 `aria-live` 과도 사용

- **WCAG:** 4.1.3
- **현상:** 토스트가 항상 `aria-live="assertive"`. 성공 메시지에도 스크린 리더 현재 발화를 중단시킴.
- **수정 방향:** 기본값 `aria-live="polite"`, 에러 시에만 `assertive`로 동적 변경

#### M-03. 헤딩 레벨 건너뜀 (h1 → h3) — ✅ 수정 완료 (탭 구조 전환 시 h2로 변경)

- **WCAG:** 1.3.1 Info and Relationships
- **수정 내용:** 탭 패널 내 섹션 헤딩(`내 정보 수정`, `연동 계정`, `내 메모`, `Danger Zone`)을 모두 `<h2>`로 변경. 시각적 크기는 `class="h6"`으로 유지.

#### M-04. 색상 대비 미달 (WCAG 1.4.3)

| 요소 | 색상 | 배경 | 대비비 | 기준 |
|------|------|------|--------|------|
| `.mypage-nickname-counter` | `#94a3b8` | `#ffffff` | 2.56:1 | 4.5:1 미달 |
| `.mypage-memo-card-date` | `#94a3b8` | `#ffffff` | 2.56:1 | 4.5:1 미달 |
| `.mypage-oauth-status-badge.is-unlinked` | `#94a3b8` | `#f8fafc` | 2.45:1 | 4.5:1 미달 |

> 참고: `.mypage-badge.join-date`(`#64748b` on `#f8fafc`)는 대비비 4.55:1로 WCAG AA 기준(4.5:1)을 통과한다.

- **수정 방향:** `#94a3b8` 사용처를 `#64748b` 이상으로 변경

#### M-05. `prefers-reduced-motion` 미지원

- **WCAG:** 2.3.3 Animation from Interactions (AAA 기준 — AA 범위 외이나 모범사례로 권장)
- **현상:** `fadeUp`, `shimmer`, `tabFadeIn` 애니메이션과 다수의 `transition`에 모션 감소 미디어 쿼리 없음.
- **수정 방향:**

```css
@media (prefers-reduced-motion: reduce) {
    *, *::before, *::after {
        animation-duration: 0.01ms !important;
        transition-duration: 0.01ms !important;
    }
}
```

#### M-06. 닉네임 카운터 `aria-describedby` 미연결

- **현상:** 글자수 카운터(`0/50`)가 input에 `aria-describedby`로 연결되지 않음.
- **수정 방향:** 카운터에 `id="nicknameCounter"`, input에 `aria-describedby="nicknameCounter"`, 카운터에 `aria-live="polite"` 적용

### 인터랙션 디자인 — 미수정

#### M-07. 닉네임 저장 중 버튼 로딩 표시 없음

- **현상:** `setFormEnabled(false)`로 비활성화되지만 버튼 텍스트 "저장하기" 유지.
- **수정 방향:** 제출 중 버튼 텍스트를 "저장 중..." 또는 스피너 아이콘으로 교체

#### M-08. "더보기" 버튼 로딩 인디케이터 없음

- **현상:** `memoMoreBtn` 클릭 시 시각 변화 없음.
- **수정 방향:** 클릭 시 "불러오는 중..." 교체 + `disabled` 처리, 완료 후 복원

#### M-09. 에러 토스트에 성공 아이콘 표시

- **현상:** 토스트 아이콘 SVG가 체크(✓) 하나만 존재. 에러 시에도 체크 아이콘 표시.
- **수정 방향:** 에러(`✕`), info(`ℹ`), 성공(`✓`) 각각의 SVG 아이콘 분리

#### M-10. 아바타 hover 효과 의미 불명확

- **현상:** 클릭 불가 요소에 `hover: scale(1.02)` 효과. `@media (hover: hover)` 조건 없음.
- **수정 방향:** 아바타 클릭 불가라면 hover 효과 제거

### 정보 구조 — 수정 완료

#### M-11. 섹션 순서 비최적 — ✅ 탭 구조 전환으로 해소

- **기존 현상:** "내 메모"가 3번째 카드로 밀려 있어 노출 우선순위 낮음.
- **수정 내용:** 2탭 구조(계정 설정 / 내 메모)로 분리. 기본 탭은 "내 메모"(핵심 가치 콘텐츠 우선), `?focus=nickname` 시에만 "계정 설정" 탭으로 진입.

#### M-12. OAuth 카드 이중 상태 전환 — ✅ 수정 완료

- **기존 현상:** `updateOAuthCards(new Map())`로 즉시 "미연동" 전환 후 API 응답 대기. 2번 깜빡임 + **셀렉터 불일치 버그** 발견.
- **수정 내용:**
  - `updateOAuthCards(new Map())` 호출 제거 — API 응답 전까지 "연동 확인 중" 초기 상태 유지
  - status 셀렉터를 `.mypage-oauth-status, .mypage-oauth-status-badge`로 보강하여 클래스 변경 후에도 매칭 보장

### 시각적 일관성 — 미수정

#### M-13. OAuth 카드 box-shadow elevation 역전

- **현상:** 내부 카드(`.mypage-oauth-card`: `0 6px 14px`)가 외부 카드(`.mypage-info-card`: `0 1px 3px`)보다 강한 그림자.
- **수정 방향:** oauth-card의 box-shadow를 `0 1px 3px rgba(15, 23, 42, 0.04)` 수준으로 조정

#### M-14. border-radius 혼용

| 요소 | 값 |
|------|-----|
| `.mypage-hero`, `.mypage-info-card` | `1rem` (16px) |
| `.mypage-oauth-card`, `.mypage-memo-card` | `0.75rem` (12px) |
| `.mypage-danger-zone` | `0.75rem` (12px) |
| `hero.css .page-hero` | `18px` |
| `card.css .card-soft` | `16px` |

- **수정 방향:** `--card-radius-lg: 1rem`, `--card-radius-sm: 0.75rem` 토큰 도입

#### M-15. 모바일 터치 타겟 44px 미달

- **현상:** `.is-empty` 연동 버튼과 Toast 닫기 버튼이 576px 이하에서 44px 미만 가능성.
- **수정 방향:** `.mypage-oauth-action` 및 `.btn-close`에 `min-height: 44px` 추가
- **참고:** 탭 버튼은 `min-height: 44px` 적용 완료.

---

## 6. Minor 이슈 — 미수정

| ID | 항목 | 설명 |
|----|------|------|
| m-01 | 카드 스태거 애니메이션 | 모든 info-card에 동일한 `animation-delay: 0.1s` — 순차 등장 효과 없음 |
| m-02 | 닉네임 공백 입력 안내 없음 | 공백만 입력 시 버튼이 왜 비활성인지 인라인 안내 부재 |
| m-03 | 연동 해제 모달 결과 설명 부재 | "해제 후 해당 계정으로 로그인 불가" 등 결과 안내 없음 |
| m-04 | primary account 안내에 탈퇴 링크 없음 | "해제하려면 회원 탈퇴를 진행해 주세요" 안내에 인라인 링크 부재 |
| m-05 | 프로필 이미지 `loading="lazy"` | Above the fold 이미지에 lazy loading 적용은 비효율 |
| m-06 | 카드 패딩 비정수 단위 | `1.1rem` 패딩이 4px 기반 그리드에서 이탈 |

---

## 7. 잘된 점

| 항목 | 설명 |
|------|------|
| 스켈레톤 UI | 히어로/메모 모두 구조를 모방한 shimmer 구현 |
| CSS 커스텀 속성 | `--mypage-*` 페이지 스코프 디자인 토큰으로 색상 관리 중앙화 |
| 데스크톱 전용 hover | `@media (hover: hover) and (pointer: fine)` 조건 적용 |
| OAuth 에러 URL 정리 | `history.replaceState`로 새로고침 시 에러 반복 방지 |
| 빈 메모 CTA | "성경 읽기에서 메모를 남겨보세요" 바로가기 |
| returnUrl 리디렉션 | 닉네임 설정 유도 → 원래 페이지 복귀 흐름 |
| 이메일 마스킹 | `ab***@domain.com` 형태 개인정보 보호 |
| ESC/backdrop 모달 닫기 | 키보드 + 마우스 양방향 닫기 지원 |
| 중복 제출 방지 | `memoLoading` 플래그 + `setFormEnabled` |
| Safe returnUrl 검증 | `//`로 시작하는 프로토콜 상대 URL 차단 방어 코드 |
| 탭 접근성 | `role="tablist/tab/tabpanel"`, `aria-selected`, `aria-controls` 완비 |
| 탭 포커스 접근성 | `focus-visible` 아웃라인으로 키보드 네비게이션 시각적 피드백 제공 |

---

## 8. 프로젝트 전체 일관성 이슈

마이페이지와 다른 페이지 간 UI 패턴 불일치 사항:

| 영역 | mypage | 다른 페이지들 | 비고 |
|------|--------|-------------|------|
| 알림 | 커스텀 토스트 | `window.alert()` | mypage가 우수 — 토스트를 공통 컴포넌트로 추출 권장 |
| 모달 | 커스텀 + ARIA + 포커스 트랩 | `window.confirm()` | mypage가 우수 — 모달을 공통 컴포넌트로 추출 권장 |
| 로딩 | skeleton shimmer | `.custom-spinner` | 패턴 불일치 — 용도별 통일 필요 |
| 빈 상태 | 텍스트 + CTA | emoji + 텍스트 | 패턴 불일치 — 표준 형태 정의 필요 |
| 색상 변수 | `--mypage-*` 6개 | 일부만 변수 사용 | 전역 색상 시스템 도입 권장 |
| border-radius | `1rem` / `0.75rem` | `18px` / `16px` | 단위 및 값 통일 필요 |
| 그림자 값 | `0 1px 3px` | `0 2px 8px` / `0 12px 12px` | 3단계 elevation 토큰 정의 권장 |
| 탭 패턴 | Segmented Control (라운드 배경) | 언더라인 탭 (community) | 용도별 차이 허용 가능하나 통일 검토 필요 |

---

## 9. 수정 우선순위 (미수정 항목만)

### 1순위: 다음 스프린트 (Major 접근성)

1. 스켈레톤에 `role="status"` + 접근성 텍스트 추가 (M-01)
2. 색상 대비 미달 요소 수정: `#94a3b8` → `#64748b` 이상 (M-04)
3. `prefers-reduced-motion` 미디어 쿼리 추가 (M-05)
4. 토스트 `aria-live` 성공/에러 분리 (M-02)
5. 닉네임 카운터 `aria-describedby` 연결 (M-06)

### 2순위: 인터랙션/일관성 개선

6. 저장/더보기 버튼 로딩 상태 표시 (M-07, M-08)
7. 토스트 아이콘 variant별 분리 (M-09)
8. 아바타 hover 효과 제거 (M-10)
9. box-shadow elevation, border-radius 통일 (M-13, M-14)
10. 모바일 터치 타겟 44px 보장 (M-15)

### 3순위: 백로그 (Minor)

11. 카드 스태거 애니메이션 차별화 (m-01)
12. 공백 닉네임 실시간 안내 (m-02)
13. 연동 해제 모달 결과 설명 추가 (m-03)
14. primary account 안내 탈퇴 링크 (m-04)
15. 프로필 이미지 `loading="lazy"` 제거 (m-05)
16. 패딩 4px 그리드 정렬 (m-06)

---

## 10. 탭 분리 구조 — ✅ 구현 완료

### 10.1 구현된 구조

```
┌─────────────────────────────┐
│  Hero 섹션 (항상 표시)        │
│  아바타 / 이름 / 뱃지 / 통계  │
└─────────────────────────────┘
  ┌──────────────┬──────────────┐
  │  계정 설정    │  내 메모 [3] │  ← Segmented Control (sticky)
  └──────────────┴──────────────┘
  - 계정 설정 탭:              - 내 메모 탭:
    내 정보 수정 폼               메모 목록
    연동 계정 (3 OAuth)           더보기 페이지네이션
    ─── 분리선 ───
    Danger Zone
```

### 10.2 구현 상세

| 항목 | 구현 내용 |
|------|----------|
| 탭 스타일 | Segmented Control 패턴 (라운드 배경 `#e8edf3` + 활성 탭 흰색 카드 + 미세 그림자) |
| ARIA 접근성 | `role="tablist/tab/tabpanel"`, `aria-selected`, `aria-controls`, `aria-labelledby` |
| 키보드 접근성 | `focus-visible` 아웃라인 (`outline: 2px solid accent`) |
| 터치 타겟 | `min-height: 44px` + `flex: 1` 균등 배분 |
| URL 딥링크 | `?tab=memo` / `?tab=settings` 쿼리 파라미터 동기화 (`history.replaceState`) |
| 스크롤 위치 보존 | 탭 전환 시 `tabScrollPositions` 객체에 저장/복원 |
| 기본 탭 로직 | `?focus=nickname` → 계정 설정, 그 외 → 내 메모 (핵심 가치 콘텐츠 우선) |
| 메모 탭 뱃지 | 메모 수가 0보다 크면 탭 라벨 옆에 건수 뱃지 표시 |
| 탭 전환 효과 | `tabFadeIn` (opacity only, 0.2s) — 위치 이동 없이 깜빡임 최소화 |
| hover | 비활성 탭에 `rgba(255,255,255,0.5)` 반투명 배경. `@media (hover: hover)` 적용 |
| OAuth 헤더 | 아이콘/제공자명/상태 뱃지를 1행 가로 배치 (`margin-left: auto`로 상태 우측 정렬) |

### 10.3 검토 배경 (분석 요약)

탭 분리 도입 전 5가지 구조 방안을 비교 분석했다:

| 기준 | 단일 스크롤 | Segmented 2탭 (채택) | 3탭 | Accordion | Anchor Nav |
|------|-----------|---------------------|-----|-----------|------------|
| 모바일 스크롤 깊이 | 매우 나쁨 | 좋음 | 좋음 | 보통 | 나쁨 |
| Discoverability | 좋음 | 보통 | 보통 | 좋음 | 좋음 |
| 구현 복잡도 | 없음 | 낮음 | 중간 | 낮음 | 낮음 |
| 모바일 라벨 공간 | 해당 없음 | 충분 | 주의 필요 | 해당 없음 | 주의 필요 |
| 메모 확장성 | 나쁨 | 좋음 | 좋음 | 나쁨 | 보통 |

**채택 근거:** NNG/Material Design 3/Apple HIG 탭 사용 조건 충족, "내 메모"와 다른 섹션의 동시 참조 필요성 매우 낮음, 커뮤니티 페이지에 기존 탭 패턴 존재.

### 10.4 장기 방향: 점진적 공개 (Progressive Disclosure)

메모 기능이 성장하면(필터, 성경 범위별 검색, 색상 분류 등):

```
마이페이지: Hero + 계정 설정 + "내 메모" 미리보기 3개 + [전체 보기 →]
/mypage/memos: 메모 전용 페이지 (필터, 검색, 전체 목록)
```

2탭 구조에서 URL만 추가하면 자연스럽게 마이그레이션 가능하다.

---

## 11. 추가 발견 버그 — ✅ 수정 완료

### BUG-01. OAuth 상태 뱃지 셀렉터 불일치 (기능 결함)

- **현상:** `updateOAuthCards()`가 `card.querySelector(".mypage-oauth-status")`로 status 요소를 찾지만, 첫 호출 시 `status.className = "mypage-oauth-status-badge is-unlinked"`로 원래 클래스를 완전히 덮어씀. 두 번째 호출(`loadOAuthAccounts` → `renderOAuthAccounts`) 시 셀렉터가 매칭되지 않아 **연동된 계정이어도 상태 뱃지가 "미연동"으로 고정**되는 결함.
- **수정 내용:**
  - 셀렉터를 `.mypage-oauth-status, .mypage-oauth-status-badge`로 보강
  - `updateOAuthCards(new Map())` 호출 제거 — 이중 상태 전환 문제(M-12)도 동시 해소

---

## 문서 검토 이력

| 일자 | 내용 |
|------|------|
| 2026-03-14 | 초안 작성 (UI/UX 전문가 검토 3인 병렬 분석) |
| 2026-03-14 | Critic 검토 반영: 색상 대비비 재계산(M-04), WCAG 2.3.3 AAA 명시(M-05), h3 목록 보완(M-03), XSS 이스케이프 범위 확대(C-02), 스크롤 깊이 산출 근거 보강(10.1), Hero sticky 단점 상세화(10.2), 기본 탭 로직 M-11과 모순 해소(10.6) |
| 2026-03-14 | 구현 반영: Critical 3건 수정 완료(C-01~C-03), 탭 분리 구현(10절), 탭 디자인 Segmented Control 개선, OAuth 헤더 1행 배치, BUG-01 셀렉터 불일치 수정, M-03/M-11/M-12 수정 완료 상태 반영 |
