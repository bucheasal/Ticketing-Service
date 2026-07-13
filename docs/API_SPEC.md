# API 명세

## 1. 개요

이 문서는 프로젝트의 API 엔드포인트 목록을 정리한 문서입니다.

- 전체 API 수: **10개**
- 원본 CSV에서 Method와 URL이 비어 있던 `new Endpoint` 행은 제외했습니다.
- 원본에 설명이 없는 항목은 `추가 작성 필요`로 표시했습니다.

## 2. 전체 API 목록

| 카테고리 | 기능 | Method | Endpoint | Path Parameter | 설명                            |
|---|---|---|---|---|-------------------------------|
| 인증 | 로그인 | `POST` | `/auth/login` | `-` | 로그인 합니다.                      |
| 검색 | 공연 전체 목록 | `GET` | `/events` | `-` | DB에 있는 모든 공연 목록을 보여줍니다        |
| 검색 | 해당 공연 조회 | `GET` | `/events/{eventId}/schedule` | `eventId` | 선택한 공연ID를 이용해서 해당 공연 일자를 얻습니다 |
| 검색 | 고객 예약 목록 | `GET` | `/reservations` | `-` | 고객이 예약한 예약 목록 조회              |
| 검색 | 고객 예약 조회 | `GET` | `/reservations/{reservationId}` | `reservationId` | 고객 예약 상세 조회                   |
| 검색 | 좌석 조회 | `GET` | `/schedules/{scheduleId}/seats` | `scheduleId` | 해당 일정에서 좌석 목록 조회              |
| 유저 | 예약 확정 | `POST` | `/reservation` | `-` | 결제가 성공하면 예약 확정으로 상태 전이합니다     |
| 유저 | 예약 취소 | `PATCH` | `/reservation/{reservationId}` | `-` | 결제까지 마친 예약을 취소합니다             |
| 유저 | 좌석 선점 | `POST` | `/seats/{seatId}` | `-` | 회원이 좌석 선점합니다                  |
| 유저 | 모의 결제 | `POST` | `/seats/{seatId}/pay` | `-` | 결제 과정을 간략하게 대체합니다.            |

## 3. 카테고리별 상세

### 3.1. 인증

#### 로그인

`POST /auth/login`

- **설명:** 로그인 합니다.
- **인증 여부:** 추가 작성 필요
- **요청 Body:** 추가 작성 필요
- **응답:** 추가 작성 필요
- **Path Parameter:** 없음

### 3.2. 검색

#### 공연 전체 목록

`GET /events`

- **설명:** DB에 있는 모든 공연 목록을 보여줍니다
- **인증 여부:** 추가 작성 필요
- **요청 Body:** 없음 또는 추가 확인 필요
- **응답:** 추가 작성 필요
- **Path Parameter:** 없음

#### 해당 공연 조회

`GET /events/{eventId}/schedule`

- **설명:** 선택한 공연ID를 이용해서 해당 공연 일자를 얻습니다
- **인증 여부:** 추가 작성 필요
- **요청 Body:** 없음 또는 추가 확인 필요
- **응답:** 추가 작성 필요
- **Path Parameter:**
  - `eventId`: 추가 작성 필요

#### 고객 예약 목록

`GET /reservations`

- **설명:** 고객이 예약한 예약 목록 조회
- **인증 여부:** 추가 작성 필요
- **요청 Body:** 없음 또는 추가 확인 필요
- **응답:** 추가 작성 필요
- **Path Parameter:** 없음

#### 고객 예약 조회

`GET /reservations/{reservationId}`

- **설명:** 고객 예약 상세 조회
- **인증 여부:** 추가 작성 필요
- **요청 Body:** 없음 또는 추가 확인 필요
- **응답:** 추가 작성 필요
- **Path Parameter:**
  - `reservationId`: 추가 작성 필요

#### 좌석 조회

`GET /schedules/{scheduleId}/seats`

- **설명:** 해당 일정에서 좌석 목록 조회
- **인증 여부:** 추가 작성 필요
- **요청 Body:** 없음 또는 추가 확인 필요
- **응답:** 추가 작성 필요
- **Path Parameter:**
  - `scheduleId`: 추가 작성 필요

### 3.3. 유저

#### 예약 확정

`POST /reservation`

- **설명:** 추가 작성 필요
- **인증 여부:** 추가 작성 필요
- **요청 Body:** 추가 작성 필요
- **응답:** 추가 작성 필요
- **Path Parameter:** 없음

#### 예약 취소

`PATCH /reservation/{reservationId}`

- **설명:** 추가 작성 필요
- **인증 여부:** 추가 작성 필요
- **요청 Body:** 추가 작성 필요
- **응답:** 추가 작성 필요
- **Path Parameter:**
  - `reservationId`: 추가 작성 필요

#### 좌석 선점

`POST /seats/{seatId}`

- **설명:** 추가 작성 필요
- **인증 여부:** 추가 작성 필요
- **요청 Body:** 추가 작성 필요
- **응답:** 추가 작성 필요
- **Path Parameter:**
  - `seatId`: 추가 작성 필요

#### 모의 결제

`POST /seats/{seatId}/pay`

- **설명:** 추가 작성 필요
- **인증 여부:** 추가 작성 필요
- **요청 Body:** 추가 작성 필요
- **응답:** 추가 작성 필요
- **Path Parameter:**
  - `seatId`: 추가 작성 필요

## 4. 보완이 필요한 사항

현재 목록만으로는 실제 구현 명세가 완전하지 않으므로 다음 내용을 추가하는 것이 좋습니다.

- 인증 필요 여부와 접근 가능한 사용자 역할
- 요청 Body 및 필드별 타입·필수 여부
- Query Parameter
- 성공 응답의 HTTP 상태 코드와 JSON 형식
- 실패 응답과 에러 코드
- 좌석 선점 만료 시간 및 중복 선점 처리 정책
- 결제 성공·실패 및 예약 확정 간 트랜잭션 정책
- 예약 취소 가능 조건과 환불 정책

## 5. 엔드포인트 표기 검토

원본에는 예약 관련 경로가 단수형과 복수형으로 혼용되어 있습니다.

- 목록·상세 조회: `/reservations`, `/reservations/{reservationId}`
- 예약 확정·취소: `/reservation`, `/reservation/{reservationId}`

REST API의 일관성을 위해 모두 `/reservations`로 통일할지 검토하는 것이 좋습니다.