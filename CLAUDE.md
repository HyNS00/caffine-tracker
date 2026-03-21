# CaFit 프로젝트 컨텍스트

## 기술 스택
- Java 21, Spring Boot 3.5.8, JPA, Gradle
- DB: MySQL (dev), H2 (CI)
- Frontend: 바닐라 HTML/CSS/JS + Chart.js (SPA)
- 인증: 세션 기반 (LoginFilter) → Spring Security 전환 예정
- CI: GitHub Actions

## 개발 문서
- `PRD.md`: 제품 요구사항 정의서 (기능 우선순위, 비기능 요구사항)
- `plan.md`: Phase별 개발 계획 (Issue 단위, 체크박스 진행 관리)

## 코드 컨벤션
- `backend-code-convention.md` 참조 (Google Java Convention 기반, 4스페이스, 120자 제한)
- 변경된 Layered Architecture: presentation / application / domain / infrastructure / global

## 커밋 & 브랜치 컨벤션
- 커밋 메시지: **한국어** 작성, prefix 사용 (feat, fix, refactor, test, chore, docs)
  - 예: `feat: 카페인 민감도 설문 API 구현`
  - 예: `refactor: 패키지 구조 변경 (controller → presentation)`
- 브랜치 네이밍: `{type}/issue-{번호}-{설명}` (plan.md의 Issue 번호 기반)
  - 예: `refactor/issue-3-base-entity`, `feature/issue-15-sensitivity`
- PR 단위: Issue 1개 = PR 1개, Phase 완료 시 사용자 확인 후 다음 Phase 진행
- 커밋 메시지에 Claude 관련 문구(Co-Authored-By 등)를 포함하지 않는다

## 완료된 리팩토링 (Phase 0)
- Issue #3: BaseEntity + BaseTimeEntity + ErrorCode 체계 ✅
- Issue #4: 패키지 구조 + Facade + Adapter + QueryDSL ✅
- Issue #5: 코드 스타일 + BCrypt + DTO 이동 (미착수)

## Spring Security 마이그레이션 계획

### 참고 프로젝트
- 경로: `/Users/hyso/Desktop/팀프로젝트/statistics-server/src/main/java/com/prism/statistics`
- 구조: OAuth2 + JWT(JWE) + Stateless 세션
- 주요 파일: SecurityConfig, TokenConfig, OAuth2SuccessHandler, OAuth2AuthenticationFilter 등

### 단계별 적용
1. **Phase 2**: SecurityConfig + BCrypt (세션 기반 유지, 기존 LoginFilter를 Security 필터 체인으로 교체)
2. **Phase 6**: JWT 토큰 방식 전환 (Next.js 프론트 분리 후)
3. **Phase 6**: OAuth2 소셜 로그인 추가

### 현재 → Security 전환 매핑
| 현재 | Security 전환 후 |
|------|-----------------|
| `LoginFilter` | Security 필터 체인 (`SecurityFilterChain`) |
| `@Login` 어노테이션 | `@AuthenticationPrincipal` 또는 커스텀 `@AuthUserId` |
| `HttpSession` 직접 관리 | Security 세션 관리 |
| 평문 비밀번호 | BCryptPasswordEncoder |
| 없음 | CORS/CSRF 설정 |

## 카페인 민감도 기능 (Phase 3, Issue #15)

### 기능 개요
- 회원가입 후 민감도 설문 (5개 질문)
- 점수 합산 → 민감도 자동 계산 (HIGH/MEDIUM/LOW)
- 민감도에 따라 User 설정값 자동 적용
- 설정 페이지에서 재측정/직접 수정 가능

### API 설계 (확정)
- `POST /api/users/sensitivity` - 민감도 설정 (최초)
- `PUT /api/users/sensitivity` - 민감도 변경
- `PUT /api/users/settings` - 세부 설정 수정
- `GET /api/users/settings` - 현재 설정 조회

### 결정 사항
- API 경로: `/api/users/...` 형식 사용
- 기존 사용자 기본 민감도: MEDIUM (자동 설정)
