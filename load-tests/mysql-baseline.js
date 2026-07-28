import http from "k6/http";
import { check } from "k6";
import exec from "k6/execution";
import { Counter, Rate, Trend } from "k6/metrics";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const SCHEDULE_ID = Number(__ENV.SCHEDULE_ID || 1);
const SCENARIO = __ENV.SCENARIO || "same-seat";

const scenarioDefinitions = {
  "same-seat": {
    expectedSuccesses: 1,
    expectedRejections: 99,
  },
  "different-seats": {
    expectedSuccesses: 100,
    expectedRejections: 0,
  },
  "overlapping-multi-seat": {
    expectedSuccesses: 1,
    expectedRejections: 99,
  },
};

const selectedScenario = scenarioDefinitions[SCENARIO];

if (!selectedScenario) {
  throw new Error(
    `지원하지 않는 SCENARIO입니다: ${SCENARIO}. ` +
      "same-seat, different-seats, overlapping-multi-seat 중 하나를 사용하세요.",
  );
}

const holdSuccess = new Counter("hold_success");
const expectedRejection = new Counter("expected_rejection");
const unexpectedResponse = new Counter("unexpected_response");
const expectedResult = new Rate("expected_result");
const holdDuration = new Trend("hold_duration", true);
const holdSuccessDuration = new Trend("hold_success_duration", true);
const holdRejectionDuration = new Trend("hold_rejection_duration", true);

export const options = {
  scenarios: {
    [SCENARIO]: {
      executor: "per-vu-iterations",
      vus: 100,
      iterations: 1,
      maxDuration: "30s",
      tags: {
        baseline: "mysql-only",
        test_scenario: SCENARIO,
      },
    },
  },
  thresholds: {
    checks: ["rate==1"],
    expected_result: ["rate==1"],
    hold_success: [`count==${selectedScenario.expectedSuccesses}`],
    expected_rejection: [`count==${selectedScenario.expectedRejections}`],
    unexpected_response: ["count==0"],
  },
  summaryTrendStats: ["avg", "min", "med", "max", "p(95)", "p(99)"],
};

function requestData(vuId) {
  switch (SCENARIO) {
    case "same-seat":
      return {
        userId: vuId,
        seatIds: [1],
      };
    case "different-seats":
      return {
        userId: vuId,
        seatIds: [vuId],
      };
    case "overlapping-multi-seat":
      return {
        userId: vuId,
        seatIds: vuId % 2 === 1 ? [1, 2] : [2, 3],
      };
    default:
      throw new Error(`처리할 수 없는 SCENARIO입니다: ${SCENARIO}`);
  }
}

function errorMessage(response) {
  try {
    return response.json("message") || "";
  } catch (_) {
    return "";
  }
}

function isExpectedSeatConflict(response) {
  if (response.status !== 400) {
    return false;
  }

  const message = errorMessage(response);
  return (
    message.includes("선점") ||
    message.includes("확정된 좌석")
  );
}

export default function () {
  const vuId = exec.vu.idInTest;
  const { userId, seatIds } = requestData(vuId);

  const response = http.post(
    `${BASE_URL}/reservations/holds`,
    JSON.stringify({
      scheduleId: SCHEDULE_ID,
      seatIds,
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

  const succeeded = response.status === 201;
  const rejected =
    SCENARIO !== "different-seats" && isExpectedSeatConflict(response);
  const expected = succeeded || rejected;

  holdSuccess.add(succeeded ? 1 : 0);
  expectedRejection.add(rejected ? 1 : 0);
  unexpectedResponse.add(expected ? 0 : 1);
  expectedResult.add(expected);
  holdDuration.add(response.timings.duration);

  if (succeeded) {
    holdSuccessDuration.add(response.timings.duration);
  }

  if (rejected) {
    holdRejectionDuration.add(response.timings.duration);
  }

  check(response, {
    "시나리오에서 기대한 응답이다": () => expected,
  });

  if (!expected) {
    console.error(
      `예상하지 못한 응답: scenario=${SCENARIO}, userId=${userId}, ` +
        `seatIds=${JSON.stringify(seatIds)}, status=${response.status}, ` +
        `body=${response.body}`,
    );
  }
}
