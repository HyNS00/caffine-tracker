# CaFit - Product Requirements Document

## 1. 제품 개요

**제품명**: CaFit (Caffeine + Fit)
**비전**: 사용자의 일일 카페인 섭취를 지능적으로 관리하고, 반감기 기반 잔존량 예측을 통해 수면 품질을 최적화하는 웹 서비스
**대상 사용자**: 커피를 자주 마시는 직장인, 카페인에 민감한 사람, 수면 품질 개선을 원하는 건강 관심층

## 2. 현재 상태

### 완료된 기능

| 항목 | 상태 |
|------|------|
| 사용자 인증 | 세션 기반 (LoginFilter), 평문 비밀번호 |
| 프리셋 음료 | 106개 (스타벅스, 이디야, 메가커피 등 15개+ 브랜드) |
| 커스텀 음료 | CRUD 완료 |
| 섭취 기록 | 프리셋/커스텀 기록 + 오늘 섭취 목록 |
| 카페인 계산 | 반감기 기반 잔존량 계산 (CaffeineDecayCalculator) |
| 음료 체크 | 특정 음료를 마셔도 되는지 SAFE/WARNING/DANGER 판정 |
| 통계 | 일간/주간 카페인 타임라인 |
| 즐겨찾기 | 프리셋/커스텀 즐겨찾기 + 순서 변경 |
| 프론트엔드 | 바닐라 HTML/CSS/JS + Chart.js |

User 엔티티에 이미 개인화 필드 존재: `dailyCaffeineLimit`(400mg), `caffeineHalfLife`(5.0h), `bedTime`(23:00), `targetSleepCaffeine`(50mg)

### 완료된 리팩토링

| 항목 | Issue | 상태 |
|------|-------|------|
| BaseEntity + BaseTimeEntity + JPA Auditing | #3 | ✅ |
| 도메인별 ErrorCode enum + ExceptionResponse | #3 | ✅ |
| 패키지 구조 전환 (Layered Architecture) | #4 | ✅ |
| Repository Adapter 패턴 | #4 | ✅ |
| Service Facade 분리 (CaffeineCheck, FavoriteBeverage) | #4 | ✅ |
| QueryDSL 의존성 + JPAQueryFactory 빈 등록 | #4 | ✅ |
| ListCrudRepository 통일 | #4 | ✅ |

### 남은 기술 부채

| 항목 | Issue |
|------|-------|
| dto/ 패키지가 아직 application/ 하위로 미이동 | #5 |
| 평문 비밀번호 → BCrypt 암호화 | #5 |
| FavoriteBeverage 테이블명 오타 | #5 |
| 미사용 import, TODO 주석 정리 | #5 |
| 테스트 부재 (컨텍스트 로드 테스트만 존재) | Phase 1 |

## 3. 기능 우선순위

### P0 - MVP 필수 (Phase 3)

| ID | 기능 | 설명 |
|----|------|------|
| F-01 | 카페인 민감도 | 온보딩 설문(5문항) → 민감도 자동 계산(HIGH/MEDIUM/LOW) → 사용자 설정 자동 적용 |
| F-02 | 수면 기록 | 수면 시간/품질 기록 → 카페인-수면 상관관계 분석 데이터 수집 |
| F-03 | 카페인 섭취 경고 | 수면 시간 기반 "카페인 마감 시간" 계산 + 알림 |

### P1 - MVP 확장 (Phase 4~5)

| ID | 기능 | Phase |
|----|------|-------|
| F-04 | 주간 이메일 리포트 | Phase 4 |
| F-05 | Next.js 프론트엔드 | Phase 5 |
| F-06 | 민감도 자동 추천 | 향후 |

### P2 - 향후 확장 (Phase 6~7)

| ID | 기능 | Phase |
|----|------|-------|
| F-07 | JWT 인증 | Phase 6 |
| F-08 | OAuth2 소셜 로그인 | Phase 6 |
| F-09 | MCP 통합 | Phase 7 |

## 4. 비기능 요구사항

| 항목 | 요구사항 |
|------|----------|
| 코드 품질 | `backend-code-convention.md` 준수 (4스페이스, 120자, depth≤2, no setter) |
| 아키텍처 | 변경된 Layered Architecture + Repository Adapter 패턴 |
| 테스트 | BDD 패턴, 한글 메서드명, H2 인메모리 DB |
| 보안 | BCrypt, Spring Security, CORS |
| CI/CD | GitHub Actions + H2 |
| 커밋 | 한국어 커밋 메시지, 이슈 번호 기반 브랜치 |

## 5. MVP 범위 외

- 모바일 앱 (네이티브 iOS/Android)
- 실시간 푸시 알림 (WebSocket)
- 다국어 지원
- 결제/구독
- 관리자 대시보드
- 바코드 스캔
- 피트니스 트래커/수면 추적 기기 연동
