# API 명세

## 1. 개요

이 문서는 프로젝트의 API 엔드포인트 목록을 정리한 문서입니다.

- 기존 후보 API 수: **10개**
- MVP API 수: **5개**
- 로그인, 예약 조회, 예약 취소, 결제 API는 MVP에서 제외했습니다.
- 사용자 식별은 로그인/JWT 없이 `X-User-Id` 요청 헤더로 처리합니다.

## 2. 전체 API 목록

| 카테고리 | 기능 | Method | Endpoint | Path Parameter | 설명 |
|---|---|---|---|---|---|
| 조회 | 공연 전체 목록 | `GET` | `/events` | `-` | DB에 있는 모든 공연 목록을 조회합니다. |
| 조회 | 공연 회차 조회 | `GET` | `/events/{eventId}/schedules` | `eventId` | 선택한 공연의 회차 목록을 조회합니다. |
| 조회 | 좌석 조회 | `GET` | `/schedules/{scheduleId}/seats` | `scheduleId` | 해당 회차의 좌석 목록과 상태를 조회합니다. |
| 예약 | 좌석 선점 | `POST` | `/reservations/holds` | `-` | 사용자가 좌석을 5분 동안 선점합니다. |
| 예약 | 예약 확정 | `POST` | `/reservations/{reservationId}/confirm` | `reservationId` | 결제 성공을 가정하고 선점된 좌석을 예약 확정합니다. |

## 3. 공통 규칙

- MVP에서는 로그인/JWT 인증을 구현하지 않습니다.
- 사용자 식별이 필요한 API는 `X-User-Id` 헤더를 사용합니다.
- 예약 관련 API는 같은 요청이 여러 번 전달될 수 있으므로 멱등성 처리 방식을 별도 정책으로 정의합니다.

## 4. 카테고리별 상세

### 4.1. 조회

#### 공연 전체 목록

`GET /events`

- **설명:** DB에 있는 모든 공연 목록을 보여줍니다
- **인증 여부:** 불필요
- **요청 Body:** 없음 또는 추가 확인 필요
- **응답:** 추가 작성 필요
- **Path Parameter:** 없음

#### 공연 회차 조회

`GET /events/{eventId}/schedules`

- **설명:** 선택한 공연ID를 이용해서 해당 공연의 회차 목록을 얻습니다
- **인증 여부:** 불필요
- **요청 Body:** 없음 또는 추가 확인 필요
- **응답:** 추가 작성 필요
- **Path Parameter:**
  - `eventId`: 추가 작성 필요

#### 좌석 조회

`GET /schedules/{scheduleId}/seats`

- **설명:** 해당 일정에서 좌석 목록 조회
- **인증 여부:** 불필요
- **요청 Body:** 없음 또는 추가 확인 필요
- **응답:** 추가 작성 필요
- **Path Parameter:**
  - `scheduleId`: 추가 작성 필요

### 4.2. 예약

#### 좌석 선점

`POST /reservations/holds`

- **설명:** `X-User-Id` 사용자가 요청한 좌석을 5분 동안 선점합니다.
- **인증 여부:** 로그인/JWT 없음. `X-User-Id` 헤더 필수
- **요청 Body:** 추가 작성 필요
- **응답:** 추가 작성 필요
- **Path Parameter:** 없음

#### 예약 확정

`POST /reservations/{reservationId}/confirm`

- **설명:** 결제 성공을 가정하고 선점 상태의 예약을 확정합니다.
- **인증 여부:** 로그인/JWT 없음. `X-User-Id` 헤더 필수
- **요청 Body:** 추가 작성 필요
- **응답:** 추가 작성 필요
- **Path Parameter:**
  - `reservationId`: 추가 작성 필요

## 5. 보완이 필요한 사항

현재 목록만으로는 실제 구현 명세가 완전하지 않으므로 다음 내용을 추가하는 것이 좋습니다.

- `X-User-Id` 헤더 처리 방식
- 요청 Body 및 필드별 타입·필수 여부
- Query Parameter
- 성공 응답의 HTTP 상태 코드와 JSON 형식
- 실패 응답과 에러 코드
- 좌석 선점 만료 시간 및 중복 선점 처리 정책
- 예약 확정 시 선점 만료 검증 정책
- 사용자당 최대 예약 좌석 수 초과 처리 정책
- Redis 적용 전후 성능 비교 API/테스트 시나리오
- 부하 테스트와 모니터링 지표

## 6. MVP 제외 API

- `POST /auth/login`
- `GET /reservations`
- `GET /reservations/{reservationId}`
- `PATCH /reservations/{reservationId}`
- `POST /seats/{seatId}/pay`
