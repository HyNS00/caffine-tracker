# CaFit 프로젝트 컨텍스트

## 기술 스택
- Java 21, Spring Boot 3.5.8, JPA, Gradle
- DB: MySQL (dev), H2 (CI)
- Frontend: 바닐라 HTML/CSS/JS + Chart.js (SPA)
- 인증: 세션 기반 (LoginFilter) → Spring Security 전환 예정
- CI: GitHub Actions

## 코드 컨벤션
- `backend-code-convention.md` 참조 (Google Java Convention 기반, 4스페이스, 120자 제한)
- 변경된 Layered Architecture: presentation / application / domain / infrastructure / global

## Spring Security 마이그레이션 계획

### 참고 프로젝트
- 경로: `/Users/hyso/Desktop/팀프로젝트/statistics-server/src/main/java/com/prism/statistics`
- 구조: OAuth2 + JWT(JWE) + Stateless 세션
- 주요 파일: SecurityConfig, TokenConfig, OAuth2SuccessHandler, OAuth2AuthenticationFilter 등

### 단계별 적용
1. **1단계**: SecurityConfig + BCrypt (세션 기반 유지, 기존 LoginFilter를 Security 필터 체인으로 교체)
2. **2단계**: JWT 토큰 방식 전환 (React 프론트 분리 시)
3. **3단계**: OAuth2 소셜 로그인 추가

### 현재 → Security 전환 매핑
| 현재 | Security 전환 후 |
|------|-----------------|
| `LoginFilter` | Security 필터 체인 (`SecurityFilterChain`) |
| `@Login` 어노테이션 | `@AuthenticationPrincipal` 또는 커스텀 `@AuthUserId` |
| `HttpSession` 직접 관리 | Security 세션 관리 |
| 평문 비밀번호 | BCryptPasswordEncoder |
| 없음 | CORS/CSRF 설정 |

## 리팩토링 계획 (컨벤션 적용)

### 상세 계획 파일
- `refactoring-plan.md` 참조

## 진행 중인 작업: 카페인 민감도 기능

### 기능 개요
- 회원가입 후 민감도 설문 (5개 질문)
- 점수 합산 → 민감도 자동 계산 (HIGH/MEDIUM/LOW)
- 민감도에 따라 User 설정값 자동 적용
- 설정 페이지에서 재측정/직접 수정 가능

### 브랜치 전략
1. `feature/caffeine-sensitivity-backend` - 백엔드 먼저
2. `feature/caffeine-sensitivity-frontend` - 프론트엔드 나중에

### API 설계 (확정)
- `POST /api/users/sensitivity` - 민감도 설정 (최초)
- `PUT /api/users/sensitivity` - 민감도 변경
- `PUT /api/users/settings` - 세부 설정 수정
- `GET /api/users/settings` - 현재 설정 조회

### 결정 사항
- API 경로: `/api/users/...` 형식 사용
- 기존 사용자 기본 민감도: MEDIUM (자동 설정)
