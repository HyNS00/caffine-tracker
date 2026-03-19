# CaFit 리팩토링 계획 (코드 컨벤션 적용)

## 현재 패키지 구조 → 목표 패키지 구조

### AS-IS (현재)
```
com.hyuns.cafit
├── auth/                  # LoginFilter, @Login
├── config/                # AppConfig, WebConfig
├── controller/            # 7개 컨트롤러
├── domain/
│   ├── beverage/          # Entity + Repository 혼재
│   ├── favorite/
│   ├── intake/
│   └── user/
├── dto/                   # 모든 DTO가 한곳에
│   ├── auth/
│   ├── beverage/
│   ├── caffeine/
│   ├── favorite/
│   ├── intake/
│   └── statistics/
├── errors/                # 예외 클래스
├── service/               # 8개 서비스
└── util/
```

### TO-BE (목표 - 변경된 Layered Architecture)
```
com.hyuns.cafit
├── presentation/                          # Interface Layer
│   ├── auth/
│   │   └── AuthController.java
│   ├── beverage/
│   │   ├── BeverageController.java
│   │   └── CustomBeverageController.java
│   ├── caffeine/
│   │   ├── CaffeineCheckController.java
│   │   ├── CaffeineIntakeController.java
│   │   └── CaffeineStatisticsController.java
│   └── favorite/
│       └── FavoriteBeverageController.java
│
├── application/                           # Application Layer
│   ├── auth/
│   │   ├── AuthService.java
│   │   └── dto/
│   │       ├── request/
│   │       │   ├── SignUpRequest.java
│   │       │   └── LoginRequest.java
│   │       └── response/
│   │           └── AuthResponse.java
│   ├── beverage/
│   │   ├── PresetBeverageService.java
│   │   ├── CustomBeverageService.java
│   │   └── dto/
│   │       ├── request/
│   │       │   ├── CustomBeverageCreateRequest.java
│   │       │   └── CustomBeverageUpdateRequest.java
│   │       └── response/
│   │           ├── PresetBeverageResponse.java
│   │           ├── CustomBeverageResponse.java
│   │           └── BeverageCategoryResponse.java
│   ├── caffeine/
│   │   ├── CaffeineCheckService.java
│   │   ├── CaffeineIntakeService.java
│   │   ├── CaffeineStatisticsService.java
│   │   ├── CaffeineDecayCalculator.java
│   │   └── dto/
│   │       ├── request/
│   │       │   └── CaffeineIntakeCreateRequest.java
│   │       └── response/
│   │           ├── CurrentCaffeineResponse.java
│   │           ├── DrinkCheckResponse.java
│   │           ├── DailyStatisticsResponse.java
│   │           └── CaffeineTimelineResponse.java
│   └── favorite/
│       ├── FavoriteBeverageService.java
│       └── dto/
│           ├── request/
│           │   ├── FavoriteCreateRequest.java
│           │   └── FavoriteOrderUpdateRequest.java
│           └── response/
│               └── FavoriteBeverageResponse.java
│
├── domain/                                # Domain Layer
│   ├── beverage/
│   │   ├── PresetBeverage.java
│   │   ├── CustomBeverage.java
│   │   ├── BeverageCategory.java
│   │   ├── BeverageType.java
│   │   └── repository/
│   │       ├── PresetBeverageRepository.java    # Interface
│   │       └── CustomBeverageRepository.java    # Interface
│   ├── user/
│   │   ├── User.java
│   │   └── repository/
│   │       └── UserRepository.java              # Interface
│   ├── intake/
│   │   ├── CaffeineIntake.java
│   │   └── repository/
│   │       └── CaffeineIntakeRepository.java    # Interface
│   └── favorite/
│       ├── FavoriteBeverage.java
│       └── repository/
│           └── FavoriteBeverageRepository.java  # Interface
│
├── infrastructure/                        # Infrastructure Layer
│   └── persistence/
│       ├── beverage/
│       │   ├── JpaPresetBeverageRepository.java
│       │   └── JpaCustomBeverageRepository.java
│       ├── user/
│       │   └── JpaUserRepository.java
│       ├── intake/
│       │   └── JpaCaffeineIntakeRepository.java
│       └── favorite/
│           └── JpaFavoriteBeverageRepository.java
│
└── global/                                # Global Layer
    ├── config/
    │   ├── AppConfig.java
    │   ├── WebConfig.java
    │   └── SecurityConfig.java            # (Security 도입 시)
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── ExceptionResponse.java         # record
    │   └── ErrorCode.java                 # interface
    ├── entity/
    │   ├── BaseEntity.java                # ID + equals/hashCode
    │   ├── CreatedAtEntity.java
    │   └── BaseTimeEntity.java
    └── security/                          # (Security 도입 시)
        ├── filter/
        ├── handler/
        └── resolver/
```

---

## 리팩토링 단계

### Phase 1: 기반 작업 (공통 엔티티 + 예외 체계)

#### 1-1. 공통 Entity 생성
- `global/entity/BaseEntity.java` 생성 (ID + equals/hashCode, HibernateProxy 처리)
- `global/entity/CreatedAtEntity.java` 생성 (@CreatedDate)
- `global/entity/BaseTimeEntity.java` 생성 (@LastModifiedDate)
- `@EnableJpaAuditing` 추가

#### 1-2. 예외 체계 리팩토링
- `ErrorCode` 인터페이스 생성
- 도메인별 ErrorCode enum 생성 (AuthErrorCode, BeverageErrorCode 등)
- `ExceptionResponse` record 생성
- `GlobalExceptionHandler`를 `ResponseEntityExceptionHandler` 상속으로 변경
- 기존 `CafitException`, `BadRequestException` 등 → 자바 기본 예외 + Custom Exception 조합으로 전환

### Phase 2: 패키지 구조 변경

#### 2-1. presentation 패키지
- `controller/` → `presentation/` 이동
- 도메인별 하위 패키지로 분리

#### 2-2. application 패키지
- `service/` → `application/` 이동
- `dto/` → 각 application 도메인 패키지 내부로 이동
- DTO 접미사: Request/Response 통일

#### 2-3. domain 패키지
- Repository를 `domain/{도메인}/repository/` 하위에 인터페이스로 정의
- Entity에서 DDL 스키마 어노테이션 제거 (@Column length, unique 등)
- Entity가 BaseEntity 또는 BaseTimeEntity를 상속하도록 변경

#### 2-4. infrastructure 패키지
- `infrastructure/persistence/{도메인}/` 하위에 JpaRepository 구현체 분리
- domain의 Repository 인터페이스를 구현하는 Adapter 클래스 생성

### Phase 3: 코드 스타일 적용

#### 3-1. Lombok 정리
- 허용: @Getter, @Builder, @NoArgsConstructor(PROTECTED), @RequiredArgsConstructor, @Slf4j
- 제거: @Setter, @Data, @AllArgsConstructor 등 (사용 중인 경우)

#### 3-2. Entity 개선
- setter 제거 → 도메인 특화 메서드 사용 (예: `changeTitle()`)
- 디미터 법칙 적용: getter로 꺼내서 비교하는 코드 → 엔티티 내부 비교 메서드로 전환
- `@Table(name = "복수형")` 명시
- 양방향 연관관계 지양

#### 3-3. 코드 품질
- depth 2 이하 유지 (early return 활용)
- else if / else 제거
- 부정형 표현 → 편의 메서드 분리
- enum 비교 → `==` 사용 + 내부 비교 메서드
- Stream: `toList()` 사용, `Objects::nonNull` 활용
- 한 줄에 `.` 하나만 사용

#### 3-4. @Transactional 정리
- 클래스 레벨 @Transactional 제거
- 메서드 레벨로 개별 지정 (readOnly 구분)

#### 3-5. 컨트롤러 정리
- 반환타입 `ResponseEntity<T>` 통일
- Request DTO에 `@Valid` 검증 추가
- DTO ↔ 도메인 변환 로직은 DTO가 보유

### Phase 4: 비밀번호 해싱 (Security 전 단계)
- BCryptPasswordEncoder Bean 등록
- AuthService 회원가입: 비밀번호 암호화 저장
- AuthService 로그인: matches() 비교로 변경
- 기존 평문 비밀번호 마이그레이션 전략 결정

### Phase 5: 테스트 작성

#### 5-1. 도메인 단위 테스트
- User, PresetBeverage, CustomBeverage, CaffeineIntake, FavoriteBeverage
- CaffeineDecayCalculator 단위 테스트
- BDD 패턴 (given/when/then), 한글 메서드명
- `@SuppressWarnings("NonAsciiCharacters")` + `@DisplayNameGeneration`

#### 5-2. 서비스 통합 테스트
- AuthService, CaffeineCheckService, CaffeineIntakeService 등
- `@SpringBootTest` + `@Sql`로 테스트 데이터 세팅
- 테스트 더블은 외부 의존성에만 BDDMockito 사용

#### 5-3. 컨트롤러 슬라이스 테스트
- `CommonControllerSliceTestSupport` 공통 클래스 생성
- `@WebMvcTest` + MockBean으로 서비스 모킹
- Spring Rest Docs 문서화 (추후)

### Phase 6: Spring Security 적용 (1단계)
- `spring-boot-starter-security` 의존성 추가
- `SecurityConfig` 작성 (SecurityFilterChain)
- 기존 `LoginFilter` → Security 필터 체인으로 교체
- `@Login` → `@AuthUserId` 커스텀 어노테이션 전환
- CORS/CSRF 설정
- 세션 기반 유지 (기존 방식과 동일하게)

### Phase 7: Spring Security 적용 (2단계 - JWT)
- React 프론트엔드 분리 후 진행
- JWT 토큰 발급/검증 구현
- Stateless 세션 전환
- Refresh Token (HTTP-only 쿠키)

### Phase 8: OAuth2 소셜 로그인
- `spring-boot-starter-oauth2-client` 의존성 추가
- Google/Kakao OAuth2 설정
- OAuth2SuccessHandler, OAuth2FailureHandler
- SocialLoginService (소셜 회원 자동 가입)

---

## 우선순위 요약

| 순서 | Phase | 설명 | 예상 난이도 |
|------|-------|------|-----------|
| 1 | Phase 1 | 공통 엔티티 + 예외 체계 | 낮음 |
| 2 | Phase 2 | 패키지 구조 변경 | 중간 (파일 이동 많음) |
| 3 | Phase 3 | 코드 스타일 적용 | 중간 |
| 4 | Phase 4 | 비밀번호 해싱 | 낮음 |
| 5 | Phase 5 | 테스트 작성 | 높음 |
| 6 | Phase 6 | Spring Security (세션) | 중간 |
| 7 | Phase 7 | JWT 전환 | 중간 |
| 8 | Phase 8 | OAuth2 | 높음 |

## 참고
- 리팩토링은 기능 개발(카페인 민감도)과 병행 가능
- Phase 1~3은 한 번에 진행하는 것을 권장 (패키지 구조 변경이 포함되므로)
- Phase 4는 독립적으로 진행 가능
- Phase 6~8은 statistics-server 프로젝트 참고
