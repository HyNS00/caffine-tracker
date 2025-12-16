# Cafit

나에게 맞춤형 카페인 트래커
---

# CaFit MVP 개발 계획

## 전체 아키텍처 개요

```
┌─────────────────────────────────────────────────────────────┐
│                        Client                                │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                    API Layer                                 │
│  BeverageController | IntakeController | UserController      │
│  CaffeineStatusController | NotificationController           │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                  Service Layer                               │
│  PresetBeverageService | CustomBeverageService               │
│  CaffeineIntakeService | CaffeineCalculationService          │
│  CaffeineSensitivityCalculator | UserService                 │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                  Domain Layer                                │
│  User | PresetBeverage | CustomBeverage | CaffeineIntake     │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                  Infrastructure                              │
│  Spring Batch | Scheduler | Notification (FCM/Email)         │
└─────────────────────────────────────────────────────────────┘
```

---

## Phase 1: 음료 조회 ✅ 완료

- PresetBeverageRepository
- PresetBeverageService
- BeverageController
- DTO (PresetBeverageResponse, BeverageCategoryResponse)

---

## Phase 2: 커스텀 음료 CRUD

**목표:** 사용자가 직접 음료를 등록/수정/삭제할 수 있다

| 구분 | 내용 |
|------|------|
| Repository | CustomBeverageRepository |
| Service | CustomBeverageService |
| Controller | CustomBeverageController |
| DTO | CustomBeverageRequest, CustomBeverageResponse |

**API 목록:**

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| POST | /api/beverages/custom | 커스텀 음료 생성 |
| GET | /api/beverages/custom | 내 커스텀 음료 목록 |
| PUT | /api/beverages/custom/{id} | 커스텀 음료 수정 |
| DELETE | /api/beverages/custom/{id} | 커스텀 음료 삭제 |


---

## Phase 3: 카페인 섭취 기록

**목표:** 음료를 마셨다고 기록할 수 있다

| 구분 | 내용 |
|------|------|
| Repository | CaffeineIntakeRepository |
| Service | CaffeineIntakeService |
| Controller | IntakeController |
| DTO | IntakeRequest, IntakeResponse, DailyIntakeResponse |

**API 목록:**

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| POST | /api/intakes | 섭취 기록 생성 |
| GET | /api/intakes/today | 오늘 섭취 기록 |
| GET | /api/intakes?from=&to= | 기간별 섭취 기록 |
| DELETE | /api/intakes/{id} | 섭취 기록 삭제 |

---

## Phase 4: 카페인 잔량 계산 (핵심 기능)

**목표:** 현재 체내 카페인 잔량과 "지금 마셔도 될까?" 판단

| 구분 | 내용 |
|------|------|
| Service | CaffeineCalculationService |
| Controller | CaffeineStatusController |
| DTO | CaffeineStatusResponse, CanDrinkResponse |

**API 목록:**

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| GET | /api/caffeine/status | 현재 카페인 상태 (잔량, 일일 섭취량) |
| GET | /api/caffeine/can-drink?caffeineAmount=150 | 지금 마셔도 되는지 판단 |
| GET | /api/caffeine/safe-time?caffeineAmount=150 | 언제부터 마셔도 되는지 |

**계산 로직 (CaffeineCalculationService):**

```
현재 잔량 = Σ (섭취량 × 0.5^(경과시간/반감기))

마셔도 되는지 = 
  1. 현재 잔량 + 신규 카페인 ≤ 일일 권장량
  2. 취침 시간 예상 잔량 ≤ 목표 수면 카페인량
```

---

## Phase 5: 설문 & 반감기 설정

**목표:** 설문을 통해 개인 카페인 민감도(반감기)를 설정한다

| 구분 | 내용 |
|------|------|
| Domain Service | CaffeineSensitivityCalculator |
| Controller | UserController (또는 SurveyController) |
| DTO | SurveyRequest, SurveyResponse |

**API 목록:**

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| POST | /api/users/survey | 설문 제출 → 반감기 계산 및 저장 |
| GET | /api/users/survey | 현재 설문 결과 조회 |

**설문 항목 (3문항):**
1. Q1: 취침 전 카페인 차단 시간
2. Q2: 수면 영향 체감
3. Q3: 자기 평가 민감도

**점수 → 반감기 매핑:**

| 총점 | 반감기 |
|------|--------|
| 0~2 | 3.5시간 |
| 3~5 | 5.0시간 |
| 6~8 | 6.5시간 |
| 9~10 | 8.0시간 |

---

## Phase 6: 회원 인증 (JWT) / session

**목표:** 로그인/회원가입으로 사용자 식별

| 구분 | 내용 |
|------|------|
| Config | SecurityConfig, JwtFilter |
| Util | JwtUtil |
| Service | AuthService |
| Controller | AuthController |
| DTO | SignupRequest, LoginRequest, LoginResponse |

**API 목록:**

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| POST | /api/auth/signup | 회원가입 |
| POST | /api/auth/login | 로그인 → JWT 발급 |
| POST | /api/auth/refresh | 토큰 갱신 |

**의존성 추가:**
```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
```

---

## Phase 7: 수면 피드백 & 학습

**목표:** 수면 품질 피드백을 받아 반감기 조정을 제안한다

| 구분 | 내용 |
|------|------|
| Entity | SleepFeedback (새로 생성) |
| Repository | SleepFeedbackRepository |
| Service | SleepFeedbackService |
| Controller | FeedbackController |
| DTO | SleepFeedbackRequest, WeeklyReviewResponse |

**새 Entity - SleepFeedback:**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| user | User (FK) | 사용자 |
| date | LocalDate | 날짜 |
| quality | SleepQuality (enum) | GOOD / NORMAL / BAD |
| memo | String (nullable) | 메모 |

**API 목록:**

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| POST | /api/feedback/sleep | 수면 피드백 제출 |
| GET | /api/feedback/weekly-review | 주간 리뷰 (패턴 분석) |

**주간 리뷰 분석 로직:**
- 수면 품질 BAD인 날의 카페인 섭취 패턴 분석
- "취침 N시간 전 섭취한 날 수면 품질이 낮았어요"
- 반감기 조정 제안

---

## Phase 8: Spring Batch - 주기적 분석

**목표:** 배치 작업으로 사용자별 카페인 패턴 분석 및 리포트 생성

| 구분 | 내용 |
|------|------|
| Config | BatchConfig |
| Job | WeeklyAnalysisJob, MonthlyReportJob |
| Tasklet/Chunk | UserCaffeineAnalysisTasklet |
| Entity | UserAnalysisReport (새로 생성) |

**의존성 추가:**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-batch'
```

**배치 작업 목록:**

| Job | 주기 | 설명 |
|-----|------|------|
| WeeklyAnalysisJob | 매주 월요일 09:00 | 주간 패턴 분석, 반감기 조정 제안 |
| MonthlyReportJob | 매월 1일 09:00 | 월간 리포트 생성 |
| DailyCleanupJob | 매일 03:00 | 오래된 데이터 정리 (선택) |

**새 Entity - UserAnalysisReport:**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| user | User (FK) | 사용자 |
| reportType | ReportType (enum) | WEEKLY / MONTHLY |
| periodStart | LocalDate | 분석 시작일 |
| periodEnd | LocalDate | 분석 종료일 |
| avgDailyCaffeine | double | 평균 일일 섭취량 |
| avgSleepQuality | double | 평균 수면 품질 |
| suggestedHalfLife | Double (nullable) | 제안 반감기 |
| createdAt | LocalDateTime | 생성 시간 |


---

## Phase 9: 알림 시스템

**목표:** 적절한 타이밍에 사용자에게 알림을 보낸다

| 구분 | 내용 |
|------|------|
| Config | NotificationConfig |
| Service | NotificationService |
| Entity | NotificationHistory (새로 생성) |
| Scheduler | NotificationScheduler |

**알림 종류:**

| 알림 | 트리거 | 내용 |
|------|--------|------|
| 카페인 차단 시간 알림 | 취침 N시간 전 | "지금부터 카페인을 피하면 숙면에 도움이 돼요" |
| 주간 리뷰 알림 | 매주 월요일 | "지난주 카페인 패턴을 확인해보세요" |
| 반감기 조정 제안 | 배치 분석 후 | "수면 패턴 분석 결과 반감기 조정을 권장해요" |

**알림 전송 방식:**

| 방식 | 용도 | 구현 난이도 |
|------|------|-------------|
| FCM (Firebase Cloud Messaging) | 모바일 푸시 | 중 |
| Email | 리포트, 중요 알림 | 하 |
| In-App | 앱 내 알림함 | 하 |


**새 Entity - NotificationHistory:**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| user | User (FK) | 사용자 |
| type | NotificationType (enum) | 알림 종류 |
| title | String | 제목 |
| message | String | 내용 |
| sentAt | LocalDateTime | 발송 시간 |
| readAt | LocalDateTime (nullable) | 읽은 시간 |

---

## Phase 10: API 문서화 & 테스트

**목표:** Swagger UI로 API 문서화, 테스트 코드 작성

| 구분 | 내용 |
|------|------|
| 의존성 | springdoc-openapi |
| Config | SwaggerConfig |
| Test | 각 레이어별 테스트 코드 |

**의존성 추가:**
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

**테스트 범위:**

| 레이어 | 테스트 종류 |
|--------|-------------|
| Repository | @DataJpaTest |
| Service | @SpringBootTest + Mockito |
| Controller | @WebMvcTest |
| 통합 | @SpringBootTest |

---