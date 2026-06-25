# CovaCova Backend

코바늘/뜨개질 취미 커뮤니티 앱의 백엔드.

## Tech Stack

- Spring Boot 4.0.3, Java 21 (Gradle)
- Spring Data JPA, Spring Security, Spring Validation, springdoc-openapi
- Spring Data Redis (refresh token 저장용)
- JWT: io.jsonwebtoken (jjwt) — access/refresh token 방식
- DB: MySQL (운영/로컬), H2 (테스트 슬라이스 전용)

## 작업 방식 (중요)

- **코드는 사용자가 직접 작성한다. Claude는 코드를 설명하거나 채팅에 텍스트로 보여주기만 하고, 소스 파일에 Edit/Write를 직접 하지 않는다.**
  - 단, 이 CLAUDE.md 같은 문서/메모 파일은 예외.
- 테스트는 기능 구현 후에 작성한다 (TDD 아님, test-after).
- 새로운 기능은 단계별 Task 리스트로 쪼개서 진행 상황을 추적한다.

## 패키지 구조

레이어드 + 도메인 패키지 혼합 구조.

```
com.covacova
├── CovacovaApplication.java
├── global/
│   ├── domain/          # BaseEntity 등 공통 엔티티
│   ├── security/
│   │   ├── auth/        # CustomUserDetails 등 인증 주체
│   │   └── jwt/         # JwtProperties, JwtTokenProvider
│   └── error/           # (예정) GlobalExceptionHandler (@RestControllerAdvice)
└── member/
    ├── domain/          # Member, MemberStatus, SkillLevel
    ├── infrastructure/  # MemberRepository(JPA), RefreshTokenRepository(Redis)
    ├── application/     # MemberService, NicknameGenerator
    ├── exception/       # DuplicateEmailException, NicknameGenerationException
    └── presentation/    # (예정) SignupController, DTO
```

기능 단위(member, 추후 다른 도메인)로 패키지를 나누고, 그 안에서 domain / infrastructure / application / exception / presentation 으로 레이어를 나눈다.

## 도메인 규칙

- **닉네임**: 회원가입 시 사용자가 입력하지 않고 `[형용사][명사][3자리 숫자]` 형태로 랜덤 생성 (예: `귀여운털뭉치001`). DB에서 `existsByNickname` 조회 후 중복이면 재시도, 최대 시도 횟수 초과 시 `NicknameGenerationException`.
- **회원 상태**: `MemberStatus` — PENDING(가입 직후) / ACTIVE / WITHDRAWN.
- **소셜 로그인**: 추후 지원 예정 (provider 설계는 아직 미정).

## 테스트

- `@DataJpaTest` / `@WebMvcTest` 등 슬라이스 테스트는 H2(testRuntimeOnly)를 써서 외부 DB 환경변수 없이 격리 실행.
- `@SpringBootTest`(전체 컨텍스트)는 `application-local.yml`이 요구하는 환경변수(`COVA_LOCAL_DB_URL` 등)가 필요 — IntelliJ Run Configuration에 env var 파일을 등록해서 사용.
