# ElSeeker 메인 화면 — 3D 우주 배경 섹션 구현 명세

## 구현 상태: ✅ 완료

기독교 성경 플랫폼 'ElSeeker'의 메인 화면(`index.html`)에 '별 헤는 밤' 감성의 3D 우주 배경 섹션이 구현되어 있습니다.

## 구조

### 상단 영역 (변경 없음)
- 히어로 배너 캐러셀 (`.home-hero`) — 슬라이드 2장, 자동 전환, 스와이프 지원
- 메뉴 카드 그리드 (`.home-menu-grid`) — 성경 / 학습 / 게임 / 커뮤니티 4개 카드

### 하단 영역 — 3D 우주 배경 섹션
- `<section class="universe-section" id="universeSection">` — 메뉴 카드 바로 아래
- `<canvas id="universeCanvas">` — Three.js WebGL 렌더링 캔버스
- 텍스트: "태초에 하나님이 천지를 창조하시니라" (창세기 1:1)

## 관련 파일

| 파일 | 역할 |
|------|------|
| `templates/index.html` | 우주 섹션 HTML + Three.js importmap (`three@0.160.0` CDN) |
| `static/css/home.css` | `.universe-section`, `.universe-fade` 스타일, 반응형 |
| `static/js/home/universe-bg.js` | Three.js 3D 파티클 시스템 모듈 |
| `static/js/index.js` | `initUniverse()` 호출, 히어로 캐러셀 초기화 |

## 핵심 기능

### Three.js 파티클 시스템 (`universe-bg.js`)
- **별 1200개**: `BufferGeometry` + `ShaderMaterial` (커스텀 vertex/fragment 셰이더)
- **반짝임**: `uTime` uniform으로 sin 기반 twinkle 애니메이션
- **Additive Blending**: 빛 겹침으로 자연스러운 글로우 효과
- **색상**: 70% 흰색, 15% 은은한 파랑, 15% 은은한 보라

### 마우스 패럴랙스
- `mousemove` 이벤트로 별 전체 미세 회전 (`MOUSE_INFLUENCE = 0.015`)
- 부드러운 추적 (lerp 0.05)
- `mouseleave` 시 원위치 복귀

### 스크롤 페이드인 트랜지션
1. **섹션 전체 등장**: `opacity: 0` + `scale(0.97)` → `.revealed` 클래스 추가 시 1.4s에 걸쳐 웅장하게 등장
2. **캔버스 등장**: 섹션 등장 후 0.3s 딜레이로 2s에 걸쳐 별이 서서히 나타남
3. **텍스트 밤안개 효과**: `filter: blur(6px)` + `translateY(30px)` → 1.2s에 걸쳐 블러 해제 + 상승
4. **순차 등장**: 출처 텍스트는 0.5s 딜레이로 시차 등장

### 성능 최적화
- `IntersectionObserver` — 화면에 보이지 않으면 렌더링 루프 스킵
- `ResizeObserver` — 반응형 캔버스 리사이징
- `devicePixelRatio` 최대 2 제한

### 반응형 (≤576px)
- 우주 섹션: 좌우 풀와이드, border-radius 제거
- 패딩 축소

## 기술 스택
- Three.js `0.160.0` (CDN importmap, 번들러 불필요)
- ES6 모듈 (`type="module"`)
- Spring Boot 3.x + Thymeleaf