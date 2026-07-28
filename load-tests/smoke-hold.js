import http from "k6/http";
import { check } from "k6";
import exec from "k6/execution";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const SCHEDULE_ID = Number(__ENV.SCHEDULE_ID || 1);

export const options = {
  scenarios: {
    hold_seats_smoke: {
      executor: "shared-iterations",
      vus: 3,
      iterations: 3,
      maxDuration: "10s",
    },
  },
  thresholds: {
    checks: ["rate==1"],
    http_req_failed: ["rate==0"],
    "http_req_duration{name:POST /reservations/holds}": ["p(95)<1000"],
  },
};

export default function () {
  // iterationInTest is 0, 1, 2, so the seeded users and seats are used once each.
  const offset = exec.scenario.iterationInTest;
  const userId = offset + 1;
  const seatId = offset + 1;

  const response = http.post(
    `${BASE_URL}/reservations/holds`,
    JSON.stringify({
      scheduleId: SCHEDULE_ID,
      seatIds: [seatId],
    }),
    {
      headers: {
        "Content-Type": "application/json",
        "X-User-Id": String(userId),
      },
      tags: {
        name: "POST /reservations/holds",
      },
    },
  );

  check(response, {
    "좌석 선점 응답이 201이다": (res) => res.status === 201,
    "응답에 reservationId가 있다": (res) => {
      if (res.status !== 201) {
        return false;
      }

      return Number(res.json("reservationId")) > 0;
    },
    "응답 좌석이 요청 좌석과 같다": (res) => {
      if (res.status !== 201) {
        return false;
      }

      const seatIds = res.json("seatIds");
      return Array.isArray(seatIds) && seatIds.length === 1 && seatIds[0] === seatId;
    },
  });

  if (response.status !== 201) {
    console.error(
      `선점 실패: userId=${userId}, seatId=${seatId}, status=${response.status}, body=${response.body}`,
    );
  }
}
