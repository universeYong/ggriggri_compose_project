import {test} from "node:test";
import * as assert from "node:assert/strict";
import {
  dailyQuestionDocId,
  kstDayOf,
  selectQuestionNumber,
} from "./dailyQuestion";

test("기준 날짜(2025-08-15)는 0번째 질문이고 0시는 한국 자정이다", () => {
  // 한국 2025-08-15 00:00 = UTC 2025-08-14 15:00
  const day = kstDayOf(new Date("2025-08-14T15:00:00Z"));
  assert.equal(day.yyyymmdd, "20250815");
  assert.equal(day.daysSinceBase, 0);
  assert.equal(day.startOfDayMillis, Date.parse("2025-08-14T15:00:00Z"));
});

test("UTC로는 전날이어도 한국 날짜로 계산한다", () => {
  // UTC 2026-11-14 23:30 = 한국 2026-11-15 08:30
  const day = kstDayOf(new Date("2026-11-14T23:30:00Z"));
  assert.equal(day.yyyymmdd, "20261115");
  assert.equal(day.startOfDayMillis, Date.parse("2026-11-14T15:00:00Z"));
});

test("한국 자정 직전은 아직 전날이다", () => {
  // 한국 2026-11-14 23:59:59 = UTC 2026-11-14 14:59:59
  const day = kstDayOf(new Date("2026-11-14T14:59:59Z"));
  assert.equal(day.yyyymmdd, "20261114");
});

test("기준 날짜로부터 지난 일수를 센다", () => {
  // 2025-08-15 -> 2026-11-15 = 457일
  const day = kstDayOf(new Date("2026-11-14T15:00:00Z"));
  assert.equal(day.daysSinceBase, 457);
});

test("문서 ID는 그룹 ID와 날짜를 밑줄로 잇는다", () => {
  assert.equal(
    dailyQuestionDocId("4qET84qBUHZCRgroPcWB", "20261115"),
    "4qET84qBUHZCRgroPcWB_20261115",
  );
});

test("질문은 일수를 개수로 나눈 나머지 순서로 돈다", () => {
  const numbers = [1, 2, 3, 4];
  assert.equal(selectQuestionNumber(numbers, 0), 1);
  assert.equal(selectQuestionNumber(numbers, 3), 4);
  assert.equal(selectQuestionNumber(numbers, 4), 1);
  // 실제 데이터에서 2026-11-15(457일)는 #002, 11-14는 #001, 11-13은 #004였다.
  assert.equal(selectQuestionNumber(numbers, 457), 2);
  assert.equal(selectQuestionNumber(numbers, 456), 1);
  assert.equal(selectQuestionNumber(numbers, 455), 4);
});

test("질문 목록이 비어 있으면 고르지 않는다", () => {
  assert.equal(selectQuestionNumber([], 10), null);
});

test("기준 날짜 이전이어도 범위 안의 질문을 고른다", () => {
  assert.equal(selectQuestionNumber([1, 2, 3, 4], -1), 4);
});
