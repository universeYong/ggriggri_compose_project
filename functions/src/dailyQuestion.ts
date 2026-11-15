// 일일 질문 생성에 쓰는 순수 계산 함수 모음. Firestore나 시간대 설정에 의존하지 않아
// 단위 테스트로 검증할 수 있다. 앱의 기존 규칙(HomeViewModel, DailyQuestionWorker)과
// 같은 결과를 내야 하므로 바꿀 때는 기존에 저장된 question_data와 어긋나지 않는지 본다.

// 질문 순번을 세는 기준 날짜(앱의 baseDate와 같다). 이 날이 0번째 질문이다.
const BASE_DATE_UTC = Date.UTC(2025, 7, 15);

// 한국 표준시(UTC+9). 서머타임이 없어 고정 오프셋으로 계산해도 된다.
const KST_OFFSET_MS = 9 * 60 * 60 * 1000;
const DAY_MS = 24 * 60 * 60 * 1000;

export type KstDay = {
  // 한국 날짜 yyyyMMdd. question_data 문서 ID의 뒷부분이다.
  yyyymmdd: string;
  // 한국 기준 그날 0시의 epoch millis. question_data._questionCreatedTime에 저장한다.
  startOfDayMillis: number;
  // 기준 날짜부터 지난 일수.
  daysSinceBase: number;
};

const pad = (value: number, length: number): string => {
  return String(value).padStart(length, "0");
};

export const kstDayOf = (now: Date): KstDay => {
  // UTC 메서드로 읽으면 KST로 옮긴 시각의 연·월·일이 그대로 나온다.
  const kst = new Date(now.getTime() + KST_OFFSET_MS);
  const year = kst.getUTCFullYear();
  const month = kst.getUTCMonth();
  const day = kst.getUTCDate();
  const dayStartUtc = Date.UTC(year, month, day);

  return {
    yyyymmdd: `${pad(year, 4)}${pad(month + 1, 2)}${pad(day, 2)}`,
    startOfDayMillis: dayStartUtc - KST_OFFSET_MS,
    daysSinceBase: Math.round((dayStartUtc - BASE_DATE_UTC) / DAY_MS),
  };
};

// 그룹마다 하루에 하나만 생기도록 문서 ID를 고정한다(앱의 dailyQuestionDocumentId와 같다).
export const dailyQuestionDocId = (
  groupId: string,
  yyyymmdd: string,
): string => {
  return `${groupId}_${yyyymmdd}`;
};

// 번호 순으로 정렬된 질문 번호 목록에서 그날의 질문을 고른다. 목록이 비면 null.
export const selectQuestionNumber = (
  sortedNumbers: number[],
  daysSinceBase: number,
): number | null => {
  if (sortedNumbers.length === 0) {
    return null;
  }
  // 기준 날짜 이전이어도 음수 인덱스가 나오지 않게 한다.
  const index =
    ((daysSinceBase % sortedNumbers.length) + sortedNumbers.length) %
    sortedNumbers.length;
  return sortedNumbers[index];
};
