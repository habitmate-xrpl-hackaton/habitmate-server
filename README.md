# 🏆 Habitmate Server

> XRPL 블록체인 기반 습관 형성 및 챌린지 플랫폼

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![XRPL](https://img.shields.io/badge/XRPL-Integrated-blue.svg)](https://xrpl.org/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시작하기](#-시작하기)
- [API 문서](#-api-문서)
- [XRPL 통합](#-xrpl-통합)
- [배포](#-배포)
- [기여하기](#-기여하기)

## 🎯 프로젝트 소개

**Habitmate Server**는 XRPL(XRP Ledger) 블록체인을 활용한 혁신적인 습관 형성 및 챌린지 플랫폼입니다. 사용자들이 개인적인 목표를 설정하고, 커뮤니티와 함께 도전하며, 블록체인 기반의 투명하고 신뢰할 수 있는 보상 시스템을 통해 지속적인 동기부여를 받을 수 있도록 설계되었습니다.

### ✨ 핵심 가치

- **🔗 블록체인 투명성**: XRPL을 통한 모든 거래와 보상의 투명한 기록
- **🤝 커뮤니티 중심**: 함께 도전하고 성장하는 사용자 커뮤니티
- **🎖️ 디지털 자격증명**: 완료한 챌린지에 대한 블록체인 기반 증명서
- **💰 실질적 보상**: XRP 기반의 실제 경제적 가치가 있는 보상

## 🚀 주요 기능

### 📊 챌린지 관리
- **개인 챌린지 생성**: 사용자 맞춤형 목표 설정 및 추적
- **큐레이션된 챌린지**: 전문가가 검증한 효과적인 챌린지
- **진행상황 추적**: 실시간 목표 달성률 모니터링
- **챌린지 완료 인증**: 블록체인 기반 완료 증명

### 💎 XRPL 블록체인 통합
- **에스크로 시스템**: 안전한 보상 관리 및 자동 배분
- **배치 결제**: 다중 사용자 동시 보상 처리
- **NFT 발행**: 성취에 대한 고유한 디지털 증명서
- **자격증명 시스템**: 블록체인 기반 스킬 및 성취 인증

### 🔐 보안 및 인증
- **Spring Security**: 강력한 애플리케이션 보안
- **JWT 토큰**: 안전한 사용자 인증 및 세션 관리
- **XRPL 지갑 통합**: 안전한 블록체인 지갑 연동

### 📈 데이터 관리
- **PostgreSQL**: 안정적인 관계형 데이터베이스
- **Spring Data JPA**: 효율적인 데이터 액세스 계층
- **Spring Modulith**: 모듈화된 아키텍처

## 🛠️ 기술 스택

### Backend Framework
- **Java 21**: 최신 LTS 버전으로 성능과 안정성 보장
- **Spring Boot 3.5.5**: 현대적인 웹 애플리케이션 프레임워크
- **Spring Security**: 인증 및 권한 관리
- **Spring Data JPA**: ORM 및 데이터 액세스

### 블록체인 통합
- **XRPL4J 3.2.1**: XRP Ledger Java 라이브러리
- **XUMM SDK**: 모바일 지갑 연동
- **WebFlux**: 비동기 블록체인 통신

### 데이터베이스
- **PostgreSQL**: 운영 환경 데이터베이스
- **H2**: 테스트 환경 인메모리 데이터베이스

### 문서화 및 API
- **SpringDoc OpenAPI**: 자동 API 문서 생성
- **Swagger UI**: 대화형 API 문서

### 개발 도구
- **Lombok**: 코드 간소화
- **Gradle**: 빌드 자동화
- **Docker**: 컨테이너화

## 🚀 시작하기

### 시스템 요구사항

- **Java**: 21 이상
- **PostgreSQL**: 13 이상
- **Gradle**: 8.0 이상
- **Docker**: (선택사항) 컨테이너 배포용

### 로컬 개발 환경 설정

1. **저장소 클론**
   ```bash
   git clone https://github.com/your-repo/habitmate-server.git
   cd habitmate-server
   ```

2. **데이터베이스 설정**
   ```bash
   # PostgreSQL 설치 및 데이터베이스 생성
   createdb habitmate_db
   ```

3. **환경 변수 설정**
   ```bash
   # application-local.yml 수정
   cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
   ```

4. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

5. **API 문서 확인**
   ```
   http://localhost:8080/swagger-ui.html
   ```

### Docker로 실행

```bash
# Docker 이미지 빌드
docker build -t habitmate-server .

# 컨테이너 실행
docker-compose -f docker-compose-local.yml up
```

## 📚 API 문서

### 주요 엔드포인트

#### 챌린지 관리
```http
GET /api/challenges              # 챌린지 목록 조회
POST /api/challenges             # 새 챌린지 생성
GET /api/challenges/{id}         # 챌린지 상세 조회
PUT /api/challenges/{id}         # 챌린지 수정
DELETE /api/challenges/{id}      # 챌린지 삭제
```

#### XRPL 블록체인
```http
POST /api/xrpl/payment/batch           # 배치 결제 처리
POST /api/xrpl/escrow/complete/batch   # 배치 에스크로 완료
POST /api/xrpl/credential/create       # 자격증명 생성
POST /api/xrpl/credential/accept       # 자격증명 수락
POST /api/xrpl/nft/mint               # NFT 발행
```

#### 큐레이션된 챌린지
```http
GET /api/challenges/curated      # 큐레이션된 챌린지 목록
```

### API 문서 접속

개발 서버 실행 후 다음 URL에서 대화형 API 문서를 확인할 수 있습니다:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🔗 XRPL 통합

### 지원하는 XRPL 기능

#### 💰 결제 시스템
- **개별 결제**: 단일 사용자 보상 지급
- **배치 결제**: 다중 사용자 동시 보상 처리
- **에스크로**: 조건부 결제 및 자동 해제

#### 🎨 NFT (Non-Fungible Token)
- **NFT 발행**: 고유한 성취 증명서 생성
- **메타데이터 관리**: IPFS 기반 NFT 정보 저장
- **소유권 추적**: 블록체인 기반 소유권 증명

#### 📜 자격증명 시스템
- **증명서 발행**: 완료된 챌린지에 대한 디지털 증명서
- **증명서 수락**: 사용자의 성취 인증
- **검증 가능**: 제3자 검증 가능한 블록체인 기록

### XRPL 네트워크 설정

```yaml
# application.yml
xrpl:
  network-id: testnet
  rpc-url: https://s.devnet.rippletest.net:51234
  central-wallet:
    address: ${CENTRAL_WALLET_ADDRESS}
    secret: ${CENTRAL_WALLET_SECRET}
```

### 블록체인 거래 예시

```java
// 배치 결제 예시
List<PaymentParams> payments = Arrays.asList(
    new PaymentParams("rUser1Address", null, new BigDecimal("10.5"), "챌린지 완료 보상"),
    new PaymentParams("rUser2Address", 12345L, new BigDecimal("15.0"), "월간 목표 달성")
);

xrplService.sendBatchPayment(payments);
```

## 🏗️ 아키텍처

### 모듈 구조

```
src/main/java/com/example/xrpl/
├── challenge/              # 챌린지 관리 모듈
│   ├── api/                # REST 컨트롤러
│   ├── application/        # 비즈니스 로직
│   └── infrastructure/     # 데이터 액세스
├── xrpl/                   # XRPL 블록체인 통합
│   ├── api/                # XRPL API 컨트롤러
│   ├── application/        # XRPL 서비스
│   ├── config/             # XRPL 설정
│   └── util/               # XRPL 유틸리티
├── common/                 # 공통 컴포넌트
└── config/                 # 전역 설정
```

### 데이터베이스 설계

```sql
-- 챌린지 테이블
CREATE TABLE challenges (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    goal_type VARCHAR(50),
    target_value INTEGER,
    reward_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 진행상황 테이블
CREATE TABLE user_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    challenge_id BIGINT,
    current_value INTEGER DEFAULT 0,
    completed_at TIMESTAMP,
    transaction_hash VARCHAR(255)
);
```

## 🚀 배포

### 환경별 설정

#### 개발 환경 (local)
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### 테스트 환경 (test)
```bash
./gradlew test
```

#### 운영 환경 (production)
```bash
java -jar build/libs/xrpl.jar --spring.profiles.active=production
```

### Docker 배포

```dockerfile
# 멀티 스테이지 빌드
FROM openjdk:21-jdk-slim as builder
COPY . .
RUN ./gradlew bootJar

FROM openjdk:21-jre-slim
COPY --from=builder build/libs/xrpl.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 환경 변수

```bash
# 데이터베이스
DATABASE_URL=jdbc:postgresql://localhost:5432/habitmate_db
DATABASE_USERNAME=habitmate_user
DATABASE_PASSWORD=secure_password

# XRPL 설정
XRPL_RPC_URL=https://s.devnet.rippletest.net:51234
CENTRAL_WALLET_ADDRESS=rYourWalletAddress
CENTRAL_WALLET_SECRET=sYourWalletSecret

# JWT 설정
JWT_SECRET=your_jwt_secret_key
```

## 🧪 테스트

### 단위 테스트 실행

```bash
./gradlew test
```

### 통합 테스트 실행

```bash
./gradlew integrationTest
```

### 테스트 커버리지 확인

```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## 📊 모니터링

### 헬스 체크
```http
GET /actuator/health
```

### 메트릭스
```http
GET /actuator/metrics
```

### 애플리케이션 정보
```http
GET /actuator/info
```

## 🤝 기여하기

### 개발 가이드라인

1. **Fork** 및 **Clone**
   ```bash
   git clone https://github.com/your-username/habitmate-server.git
   ```

2. **Feature Branch** 생성
   ```bash
   git checkout -b feature/새로운-기능
   ```

3. **커밋 컨벤션** 준수
   ```bash
   git commit -m "feat: 새로운 기능 추가"
   git commit -m "fix: 버그 수정"
   git commit -m "docs: 문서 업데이트"
   ```

4. **Pull Request** 생성

### 코드 스타일

- **Java**: Google Java Style Guide 준수
- **테스트**: Given-When-Then 패턴 사용
- **문서화**: JavaDoc 및 README 업데이트

## 📄 라이선스

이 프로젝트는 [MIT 라이선스](LICENSE) 하에 배포됩니다.

## 📞 연락처

- **프로젝트 관리자**: [your-email@example.com](mailto:your-email@example.com)
- **이슈 제보**: [GitHub Issues](https://github.com/your-repo/habitmate-server/issues)
- **문의사항**: [Discussions](https://github.com/your-repo/habitmate-server/discussions)

## 🙏 감사의 말

- [XRPL.org](https://xrpl.org/) - XRP Ledger 플랫폼
- [Spring Boot](https://spring.io/projects/spring-boot) - 웹 프레임워크
- [PostgreSQL](https://www.postgresql.org/) - 데이터베이스
- [OpenAPI](https://swagger.io/specification/) - API 문서화

---

**Habitmate Server**와 함께 더 나은 습관을 만들어가세요! 🌱
