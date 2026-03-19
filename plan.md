# CaFit 개발 계획 (Development Plan)

> Issue 번호 = GitHub Issue 번호 (기존 #1~#8 사용됨, 새 Issue는 #9부터)
> 각 Phase 완료 시 사용자 확인 후 체크(✅)하고 다음으로 진행

---

## Phase 0: Foundation (리팩토링 + 컨벤션 적용)

**목표**: 새 기능 추가 전 코드 기반 정비

### Issue #3: BaseEntity & 에러코드 체계 ✅
**Branch**: `refactor/issue-3-base-entity` | **PR**: #7

- [x] `global/entity/BaseEntity.java` 생성 (ID + equals/hashCode, HibernateProxy 처리)
- [x] `global/entity/BaseTimeEntity.java` 생성 (@CreatedDate, @LastModifiedDate)
- [x] `@EnableJpaAuditing` 설정
- [x] 도메인별 ErrorCode enum 생성 (AuthErrorCode, BeverageErrorCode 등)
- [x] `GlobalExceptionHandler` 리팩토링 → `ExceptionResponse` 응답 포맷
- [x] 기존 엔티티가 BaseEntity/BaseTimeEntity 상속하도록 수정

### Issue #4: 패키지 구조 변경 + Service Facade + Repository Adapter + QueryDSL ✅
**Branch**: `refactor/issue-4-package-restructure` | **PR**: #8

- [x] controller/ → presentation/{도메인}/ 이동
- [x] service/ → application/{도메인}/ 이동
- [x] domain/{도메인}/repository/ 인터페이스 정의
- [x] infrastructure/persistence/ → JpaRepository + RepositoryAdapter 구현
- [x] config/ → global/config/, auth/ → global/security/
- [x] CaffeineCheckFacade, FavoriteBeverageFacade 분리
- [x] QueryDSL 의존성 추가 + JPAQueryFactory 빈 등록
- [x] ListCrudRepository로 통일
- [x] BeverageType.isPreset() 추가

### Issue #5: 코드 스타일 + BCrypt + DTO 이동 (Closes #6)
**Branch**: `refactor/issue-5-code-style-bcrypt`

- [ ] FavoriteBeverage 테이블명 오타 수정
- [ ] BeverageController 미사용 import 제거
- [ ] CaffeineStatisticsService TODO 주석 정리
- [ ] dto/ → application/{도메인}/dto/ 이동
- [ ] BCryptPasswordEncoder 빈 등록
- [ ] AuthService.signup() 비밀번호 암호화 적용
- [ ] User.isPasswordMatch() → BCrypt matches() 사용
- [ ] 기존 사용자 마이그레이션 전략 (최초 로그인 시 변환 또는 일괄 변환)

---

## Phase 1: 테스트 인프라

**목표**: 리팩토링된 구조 기준 테스트 기반 구축

### Issue #9: 테스트 인프라 셋업
**Branch**: `test/issue-9-test-infrastructure`

- [ ] `@IntegrationTest` 커스텀 어노테이션 생성
  - @SpringBootTest + @ActiveProfiles("test") + CleanupExecutionListener
- [ ] `CleanupExecutionListener` 구현 (매 테스트 전 cleanup.sql 실행)
- [ ] `sql/schema.sql`, `sql/cleanup.sql` 작성 (H2 호환)
- [ ] 도메인별 테스트 데이터 SQL 파일 구성 (sql/auth/, sql/beverage/ 등)
- [ ] `@WithLoginUser` 커스텀 어노테이션 (세션 기반 인증 컨텍스트 설정)
- [ ] test용 application.yml 설정 (H2, ddl-auto: none, sql init)

### Issue #10: Domain 단위 테스트
**Branch**: `test/issue-10-domain-unit-tests`

- [ ] UserTest, CaffeineIntakeTest, PresetBeverageTest, CustomBeverageTest, FavoriteBeverageTest
- [ ] CaffeineDecayCalculatorTest (다양한 시간값)
- [ ] BDD 패턴, 한글 메서드명, @DisplayNameGeneration

### Issue #11: Service 통합 테스트
**Branch**: `test/issue-11-service-tests`

- [ ] @IntegrationTest 활용
- [ ] AuthServiceTest, CaffeineCheckServiceTest, CaffeineIntakeServiceTest, CaffeineStatisticsServiceTest
- [ ] @Sql로 테스트 데이터 셋업, CleanupExecutionListener로 격리

### Issue #12: Controller 슬라이스 테스트
**Branch**: `test/issue-12-controller-tests`

- [ ] `CommonControllerSliceTestSupport` 베이스 클래스 생성
  - standalone MockMvc + Mock 자동 주입 + ResetMockTestExecutionListener
- [ ] AuthController, CaffeineCheckController, CaffeineIntakeController, BeverageController 테스트
- [ ] @WithLoginUser + @WebMvcTest + BDDMockito

---

## Phase 2: Spring Security (세션 기반)

**목표**: LoginFilter → Spring Security 전환 (세션 유지, JWT는 아직 아님)

### Issue #13: SecurityFilterChain 설정
**Branch**: `feature/issue-13-security-config`

- [ ] spring-boot-starter-security 의존성 추가
- [ ] SecurityConfig.java 작성 (각 라인마다 한국어 주석)
- [ ] URL 접근 권한: 인증 불필요(/api/auth/**, 정적 리소스) vs 인증 필요(/api/**)
- [ ] UserDetailsService + CustomUserDetails 구현
- [ ] LoginFilter, LoginUserArgumentResolver 제거

### Issue #14: @Login → @AuthUserId 마이그레이션
**Branch**: `feature/issue-14-auth-annotation`

- [ ] AuthUserId 어노테이션 + ArgumentResolver 생성
- [ ] 모든 컨트롤러 @Login → @AuthUserId 교체
- [ ] 기존 auth 패키지 제거

---

## Phase 3: 핵심 기능

**목표**: 민감도 설문 + 수면 기록 + 카페인 경고

### Issue #15: 카페인 민감도 구현
**Branch**: `feature/issue-15-sensitivity`

- [ ] CaffeineSensitivity enum (HIGH/MEDIUM/LOW) + 프리셋 값 매핑
  - HIGH: limit=200mg, halfLife=7.0h, targetSleep=30mg
  - MEDIUM: limit=400mg, halfLife=5.0h, targetSleep=50mg
  - LOW: limit=600mg, halfLife=3.5h, targetSleep=80mg
- [ ] User 엔티티에 sensitivity 필드 추가 (기본값: MEDIUM)
- [ ] 설문 점수 계산 로직 (5문항, 각 1~5점)
- [ ] API 구현: POST/PUT /api/users/sensitivity, GET/PUT /api/users/settings
- [ ] User.applySensitivity(), User.updateSettings() 도메인 메서드
- [ ] 기존 사용자 MEDIUM 기본 설정 마이그레이션
- [ ] 단위 + 통합 + 슬라이스 테스트

### Issue #16: 수면 기록 기능
**Branch**: `feature/issue-16-sleep-record`

- [ ] SleepRecord 엔티티 (sleepTime, wakeTime, sleepQuality, memo)
- [ ] domain/sleep/repository/ 인터페이스 + Adapter 구현
- [ ] SleepRecordService (기록 CRUD + 주간 요약)
- [ ] API: POST /api/sleep, GET /api/sleep (기간 조회), PUT /api/sleep/{id}
- [ ] 카페인 잔존량 ↔ 수면 품질 상관관계 조회 API
- [ ] 단위 + 통합 테스트

### Issue #17: 카페인 경고 강화
**Branch**: `feature/issue-17-warning`

- [ ] CaffeineWarningService: 현재 잔존량 + 민감도 + 취침시간 기반 "마지막 안전 섭취 시간" 계산
- [ ] CurrentCaffeineResponse에 lastSafeCaffeineTime 필드 추가
- [ ] GET /api/caffeine/warning 엔드포인트
- [ ] 프론트엔드 토스트 메시지 연동

---

## Phase 4: 이메일

**목표**: 이메일 인프라 + 주간 카페인 리포트

### Issue #18: 이메일 인프라
**Branch**: `feature/issue-18-email-setup`

- [ ] spring-boot-starter-mail 의존성 추가
- [ ] Gmail SMTP 설정 (application-secret.yaml, 앱 비밀번호 사용)
- [ ] EmailSender 인터페이스 + SmtpEmailSender 구현
- [ ] HTML 이메일 템플릿 작성

### Issue #19: 주간 리포트 스케줄러
**Branch**: `feature/issue-19-weekly-report`

- [ ] @Scheduled 기반 주간 실행 (매주 월요일 오전 9시)
- [ ] 사용자별 주간 통계 집계 (총 카페인, 일일 평균, 상위 음료, 초과 일수, 수면 품질 요약)
- [ ] User에 emailNotificationEnabled 필드 추가
- [ ] 실패 처리 (로깅, 재시도 없음)
- [ ] 테스트 (MockMailSender)

---

## Phase 5: Next.js 프론트엔드 전환

**목표**: 바닐라 JS → Next.js + Tailwind CSS

### Issue #20: Next.js 프로젝트 셋업
**Branch**: `feature/issue-20-nextjs-setup`

- [ ] Next.js + TypeScript 프로젝트 초기화 (frontend/ 디렉토리)
- [ ] Tailwind CSS 설정
- [ ] API 클라이언트 모듈 (fetch wrapper)
- [ ] 환경변수 설정 (NEXT_PUBLIC_API_URL 등)
- [ ] 공통 레이아웃 컴포넌트 (App Router layout.tsx)

### Issue #21: CORS 설정
**Branch**: `feature/issue-21-cors-config`

- [ ] SecurityConfig 내 CORS 설정 (각 라인 한국어 주석)
  - CORS란 무엇이고 브라우저가 왜 강제하는지
  - Preflight(OPTIONS) 요청이란
  - allowCredentials가 세션 쿠키에 왜 필요한지
- [ ] Next.js 개발 서버 ↔ Spring 백엔드 통신 테스트

### Issue #22: Next.js 페이지 구현
**Branch**: `feature/issue-22-nextjs-pages`

- [ ] 로그인/회원가입
- [ ] 대시보드 (현재 카페인 상태 + 타임라인 차트)
- [ ] 음료 선택 + 섭취 기록
- [ ] 통계 (일간/주간)
- [ ] 즐겨찾기 관리
- [ ] 설정 (민감도 설문 + 수동 설정)
- [ ] 커스텀 음료 CRUD
- [ ] 기존 바닐라 JS 파일 정리

---

## Phase 6: JWT + OAuth2

**목표**: SPA를 위한 Stateless 인증

### Issue #23: JWT 토큰 시스템
**Branch**: `feature/issue-23-jwt`

- [ ] jjwt 의존성 추가
- [ ] JwtTokenProvider (생성/검증/추출)
- [ ] JwtAuthenticationFilter (OncePerRequestFilter)
- [ ] SecurityConfig 업데이트 (STATELESS)
- [ ] Refresh Token → HttpOnly Cookie
- [ ] 각 라인 한국어 주석 (JWT 플로우, Access/Refresh 분리 이유, 쿠키 보안 플래그)

### Issue #24: OAuth2 소셜 로그인
**Branch**: `feature/issue-24-oauth2`

- [ ] spring-boot-starter-oauth2-client 의존성
- [ ] Google/Kakao OAuth2 설정
- [ ] OAuth2SuccessHandler → JWT 발급
- [ ] SocialLoginService (사용자 조회/생성)
- [ ] User에 provider, providerId 필드 추가
- [ ] Next.js 소셜 로그인 버튼

---

## Phase 7: MCP

**목표**: AI 터미널 연동

### Issue #25: MCP 서버
**Branch**: `feature/issue-25-mcp`

- [ ] MCP 스펙 조사
- [ ] Tool 정의: get_caffeine_status, can_i_drink_coffee, get_today_intake
- [ ] MCP 서버 엔드포인트 구현
- [ ] 인증 (API 키 또는 사용자 토큰)

---

## 의존성 그래프

```
Phase 0 (Foundation)
  └→ Phase 1 (Tests)
       └→ Phase 2 (Security)
            └→ Phase 3 (Sensitivity + Sleep + Warning)
                 └→ Phase 4 (Email)
                      └→ Phase 5 (Next.js + CORS)
                           └→ Phase 6 (JWT + OAuth2)
                                └→ Phase 7 (MCP)
```

Phase 3의 Issue #15~#17은 Phase 2 완료 후 병렬 진행 가능.

---

## 리스크 및 대응

| 리스크 | 대응 |
|--------|------|
| BCrypt 마이그레이션 시 기존 로그인 실패 | 최초 로그인 시 lazy 변환 또는 일괄 마이그레이션 |
| Spring Security 학습 곡선 | 가장 단순한 세션 기반부터, 모든 라인에 주석 |
| CORS 이해 부족 | Issue #21에서 교육적 주석으로 개념 설명 |
| Next.js 전환 범위 비대화 | 기존 기능 동일 구현 우선, 새 기능 추가 금지 |
