# MySQL-only k6 baseline

Redis 적용 전 MySQL 비관적 락 구현의 기준치를 측정한다.

## 선정한 시나리오

| SCENARIO | 요청 | 기대 결과 | 목적 |
|---|---|---|---|
| `same-seat` | 사용자 100명이 좌석 1을 각각 선점 | 성공 1, 정상 거절 99 | 동일 좌석 경합과 중복 선점 방지 |
| `different-seats` | 사용자 100명이 좌석 1~100을 각각 선점 | 성공 100 | 무경합 처리량과 사용자 락·좌석 락 비용 |
| `overlapping-multi-seat` | 홀수 VU는 `[1,2]`, 짝수 VU는 `[2,3]` 선점 | 성공 1, 정상 거절 99 | 다중 좌석 원자성과 부분 선점 방지 |

다중 좌석 시나리오는 모든 사용자가 같은 `[1,2]`를 요청하는 대신, 좌석 2만
겹치는 두 요청 집합을 사용한다. 그래야 실패한 트랜잭션이 좌석 1 또는 3을
부분적으로 남기는지도 검증할 수 있다.

다음 두 항목은 성능 기준치에서 제외한다.

- 한 사용자가 3개 이상 선점: 최대 2좌석 정책의 기능·동시성 테스트다. 실제
  처리량을 대표하지 않으며 기존 `ReservationConcurrencyTest`에서 검증한다.
- 만료된 좌석 재선점: 5분 경과가 필요한 시간 의존 기능 테스트다. 락 처리량
  기준치보다 clock을 제어하는 통합 테스트에 적합하다.

## 사전 조건

- 애플리케이션은 `localhost:8080`에서 실행한다.
- `src/main/resources/db/data.sql`로 생성된 회차 1과 좌석 1~100을 사용한다.
- 각 실행 전에 데이터를 초기화한다.
- 테스트 중 다른 요청을 보내지 않는다.

## 실행 방법

각 시나리오 실행 직전에 데이터를 초기화한다.

```bash
docker compose exec -T mysql \
  mysql -uroot -p"$DB_PASSWORD" ticketing \
  < load-tests/sql/reset.sql
```

락 관련 MySQL 누적값을 실행 전 기록한다.

```bash
docker compose exec -T mysql \
  mysql -uroot -p"$DB_PASSWORD" ticketing \
  < load-tests/sql/mysql-status.sql
```

시나리오를 하나만 선택해서 실행한다.

```bash
SCENARIO=same-seat k6 run load-tests/mysql-baseline.js
SCENARIO=different-seats k6 run load-tests/mysql-baseline.js
SCENARIO=overlapping-multi-seat k6 run load-tests/mysql-baseline.js
```

다른 서버 주소는 `BASE_URL`로 지정한다.

```bash
BASE_URL=http://localhost:8080 \
SCENARIO=same-seat \
k6 run load-tests/mysql-baseline.js
```

k6가 설치되지 않았다면 Docker로 실행한다.

```bash
docker run --rm \
  -v "$PWD/load-tests:/scripts" \
  -e BASE_URL=http://host.docker.internal:8080 \
  -e SCENARIO=same-seat \
  grafana/k6 run /scripts/mysql-baseline.js
```

테스트 직후 MySQL 누적값을 다시 조회한다. 실행 전후의 차이를 기록해야 한다.

```bash
docker compose exec -T mysql \
  mysql -uroot -p"$DB_PASSWORD" ticketing \
  < load-tests/sql/mysql-status.sql
```

마지막으로 시나리오별 정합성을 검증한다.

```bash
docker compose exec -T mysql \
  mysql -uroot -p"$DB_PASSWORD" ticketing \
  < load-tests/sql/verify-same-seat.sql
```

검증 파일은 다음과 같다.

- `same-seat`: `load-tests/sql/verify-same-seat.sql`
- `different-seats`: `load-tests/sql/verify-different-seats.sql`
- `overlapping-multi-seat`: `load-tests/sql/verify-overlapping-multi-seat.sql`

## 결과 해석

k6 결과에서 다음 값을 기록한다.

- `hold_success`: 성공 건수와 초당 성공 건수
- `expected_rejection`: 예상된 좌석 충돌 건수
- `unexpected_response`: 예상하지 못한 실패 건수. 반드시 0이어야 한다.
- `hold_duration`: 전체 응답의 평균, p95, p99
- `hold_success_duration`: 성공 요청의 평균, p95, p99
- `hold_rejection_duration`: 정상 거절의 평균, p95, p99
- `http_reqs`: 발생 요청 수와 평균 요청률

이 테스트는 요청 하나가 트랜잭션 하나에 대응하므로 `hold_success`의 초당 값은
API 관점의 성공 TPS로 사용할 수 있다. MySQL의 실제 트랜잭션 수는 내부 쿼리와
rollback을 포함하므로 `Com_commit`, `Com_rollback` 실행 전후 차이를 별도로
기록한다.

`Innodb_row_lock_time`, `Innodb_row_lock_waits`, `Innodb_deadlocks`도 MySQL 서버
시작 이후의 누적값이다. 반드시 테스트 직전과 직후의 차이로 비교한다.

현재 API는 좌석 충돌을 `400 Bad Request`로 반환하므로 응답 메시지를 사용해
정상 거절을 분류한다. 이후 충돌 응답을 `409 Conflict`와 안정적인 error code로
변경하면 k6 분류 조건도 함께 변경해야 한다.

## 기준치 기록표

각 시나리오는 DB를 초기화한 뒤 3회 실행했으며, 아래 표에는 회차별 원본 결과를
기록한다. 현재 MySQL-only 기준값은 3회 중앙값을 사용한다. 회차가 진행될수록
응답시간이 감소하는 warm-up 경향이 확인되었으므로 Redis 전후를 비교할 때는
별도 warm-up 1회를 폐기한 후 측정 3회의 중앙값을 사용한다.

| 시나리오 | 회차 | 성공 | 정상 거절 | 예상 밖 실패 | 평균(ms) | p95(ms) | p99(ms) | 성공 TPS | DB commit 증분 | rollback 증분 | lock wait 증분 | lock time 증분(ms) | deadlock 증분 | 정합성 |
|---|---:|---:|------:|--------:|-------:|--------:|--------:|-------:|-------------:|------------:|-------------:|-----------------:|------------:|-----|
| same-seat | 1 | 1 | 99 | 0 | 91.08 | 131.22 | 134.12 | 7.11 | 1 | 99 | 99 | 872 | 0 | 통과 |
| same-seat | 2 | 1 | 99 | 0 | 91.15 | 126.30 | 128.56 | 7.34 | 1 | 99 | 99 | 791 | 0 | 통과 |
| same-seat | 3 | 1 | 99 | 0 | 91.75 | 139.48 | 142.00 | 6.57 | 1 | 99 | 99 | 849 | 0 | 통과 |
| different-seats | 1 | 100 | 0 | 0 | 98.34 | 145.37 | 146.80 | 654.96 | 100 | 0 | 0 | 0 | 0 | 통과 |
| different-seats | 2 | 100 | 0 | 0 | 64.25 | 110.08 | 111.44 | 865.60 | 100 | 0 | 0 | 0 | 0 | 통과 |
| different-seats | 3 | 100 | 0 | 0 | 56.72 | 93.02 | 94.12 | 1009.72 | 100 | 0 | 0 | 0 | 0 | 통과 |
| overlapping-multi-seat | 1 | 1 | 99 | 0 | 68.96 | 107.97 | 111.52 | 8.53 | 1 | 99 | 147 | 822 | 0 | 통과 |
| overlapping-multi-seat | 2 | 1 | 99 | 0 | 53.91 | 85.80 | 88.12 | 10.84 | 1 | 99 | 144 | 645 | 0 | 통과 |
| overlapping-multi-seat | 3 | 1 | 99 | 0 | 50.51 | 80.10 | 82.50 | 11.44 | 1 | 99 | 142 | 602 | 0 | 통과 |
