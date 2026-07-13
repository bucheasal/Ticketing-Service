# AGENTS.md

## Project

- 사용자 트래픽이 순간적으로 급증하는 지정석 티켓팅 서비스 구현.
- 동시 요청에도 중복 예약 방지하고 Redis 적용 전 후 성능 비교까지 수행한다

동시성 제어. Redis, 대기열, 부하 테스트 등의 학습을 목표로 한다

자세한 프로젝트 설명은 다음 문서를 참고한다.

- 프로젝트 개요: `docs/PROJECT_OVERVIEW.md`

[//]: # (- 시스템 구조: `docs/ARCHITECTURE.md`)
- API 명세: `docs/API_SPEC.md`

[//]: # (- 도메인 정책: `docs/DOMAIN_POLICY.md`)

[//]: # (- 에러 정책: `docs/ERROR_POLICY.md`)
- DB 스키마: `src/main/resources/db/schema.sql`

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- MySQL
- Gradle
- Redis
- JWT Authentication (추후 추가)

## Development Rules

## Task Procedure

작업을 시작하기 전에 다음을 확인한다.

1. 관련 API 명세
2. 관련 도메인 정책
3. 기존 Entity, Repository, Service, Controller
4. `schema.sql`의 실제 테이블 구조
5. 관련 테스트 코드

작업 완료 후 다음을 확인한다.

1. 프로젝트 빌드
2. 기존 테스트 실행
3. 새 기능에 대한 테스트 추가
4. API 명세와 구현의 일치 여부 확인