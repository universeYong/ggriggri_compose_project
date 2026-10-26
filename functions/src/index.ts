import {setGlobalOptions} from "firebase-functions";
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

setGlobalOptions({maxInstances: 10});
admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();
const userCollection = db.collection("user_data");

const PRUNE_ERROR_CODES = new Set([
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
]);

type FirestoreData = Record<string, unknown>;

const toStringData = (
  data: Record<string, unknown>,
): Record<string, string> => {
  const result: Record<string, string> = {};
  for (const [key, value] of Object.entries(data)) {
    if (value === undefined || value === null) continue;
    result[key] = String(value);
  }
  return result;
};

const normalizeTokens = (raw: unknown): string[] => {
  if (Array.isArray(raw)) {
    return raw.filter((token): token is string => {
      return typeof token === "string" && token.length > 0;
    });
  }
  if (typeof raw === "string" && raw.length > 0) {
    return [raw];
  }
  return [];
};

// 사용자 ID 배열 필드를 읽을 때 쓴다. 형변환(as string[]) 대신 문자열이 아니거나
// 빈 원소를 걸러서, 잘못 저장된 값이 doc(id) 호출로 넘어가지 않게 한다.
const normalizeIds = (raw: unknown): string[] => {
  if (!Array.isArray(raw)) {
    return [];
  }
  return raw.filter((id): id is string => {
    return typeof id === "string" && id.length > 0;
  });
};

const chunk = <T>(items: T[], size: number): T[][] => {
  const chunks: T[][] = [];
  for (let i = 0; i < items.length; i += size) {
    chunks.push(items.slice(i, i + size));
  }
  return chunks;
};

const getUserNameById = async (userId: string): Promise<string> => {
  if (!userId) {
    return "";
  }

  const userSnap = await userCollection.doc(userId).get();
  return String(userSnap.data()?._userName ?? "").trim();
};

// 그룹 문서가 없으면 null, 있으면 group_data._groupUserDocumentID 멤버 목록.
const getGroupMemberIds = async (
  groupId: string,
): Promise<string[] | null> => {
  const groupSnap = await db.collection("group_data").doc(groupId).get();
  if (!groupSnap.exists) {
    return null;
  }
  return normalizeIds(groupSnap.data()?._groupUserDocumentID);
};

const pruneInvalidTokens = async (
  invalidTokens: string[],
): Promise<void> => {
  const uniqueInvalidTokens = [...new Set(invalidTokens)];
  if (uniqueInvalidTokens.length === 0) {
    return;
  }

  // 전체 user_data를 스캔하지 않고, 무효 토큰을 가진 문서만 찾는다.
  // array-contains-any는 한 번에 최대 30개 값까지 받는다.
  const snapshots = await Promise.all(
    chunk(uniqueInvalidTokens, 30).map((tokenChunk) => {
      return userCollection
        .where("_userFcmToken", "array-contains-any", tokenChunk)
        .get();
    }),
  );

  const touchedDocs = new Map<string, admin.firestore.DocumentReference>();
  for (const snapshot of snapshots) {
    for (const doc of snapshot.docs) {
      touchedDocs.set(doc.id, doc.ref);
    }
  }

  const updates = [...touchedDocs.values()].map((ref) => {
    return ref.update({
      _userFcmToken:
        admin.firestore.FieldValue.arrayRemove(...uniqueInvalidTokens),
    });
  });

  await Promise.all(updates);
  logger.info("Pruned invalid FCM tokens", {
    invalidCount: uniqueInvalidTokens.length,
    updatedUserDocs: updates.length,
  });
};

const getUserTokens = async (
  userIds: string[],
  excludedUserIds: Set<string> = new Set(),
): Promise<string[]> => {
  const filteredUserIds = [...new Set(userIds)].filter((userId) => {
    return userId && !excludedUserIds.has(userId);
  });

  if (filteredUserIds.length === 0) {
    return [];
  }

  const refs = filteredUserIds.map((userId) => {
    return db.collection("user_data").doc(userId);
  });

  const snapshots = await Promise.all(
    chunk(refs, 300).map((refChunk) => db.getAll(...refChunk)),
  );

  const allTokens = snapshots.flatMap((snapChunk) => {
    return snapChunk.flatMap((snap) => {
      return normalizeTokens(snap.data()?._userFcmToken);
    });
  });

  return [...new Set(allTokens)];
};

const sendPushToTokens = async (
  tokens: string[],
  title: string,
  body: string,
  data: Record<string, unknown>,
): Promise<void> => {
  if (tokens.length === 0) {
    logger.info("No tokens to send");
    return;
  }

  // data 전용 메시지로 보낸다. notification 페이로드가 있으면 앱이 백그라운드일 때
  // 시스템이 직접 알림을 띄워서, 앱의 알림 설정(타입별 ON/OFF)과 채널이 무시된다.
  // title/body는 data에 담고, 앱(NotificationHandler)이 읽어서 표시한다.
  // priority high: 기기가 Doze 상태여도 data 메시지가 바로 전달되도록 한다.
  const payload = toStringData({...data, title, body});

  let successCount = 0;
  let failureCount = 0;
  const invalidTokens: string[] = [];

  // sendEachForMulticast는 한 번에 최대 500개 토큰까지만 받는다.
  for (const tokenChunk of chunk(tokens, 500)) {
    const message: admin.messaging.MulticastMessage = {
      tokens: tokenChunk,
      data: payload,
      android: {priority: "high"},
    };

    const result = await messaging.sendEachForMulticast(message);
    successCount += result.successCount;
    failureCount += result.failureCount;

    result.responses.forEach((response, index) => {
      if (!response.success && response.error) {
        if (PRUNE_ERROR_CODES.has(response.error.code)) {
          invalidTokens.push(tokenChunk[index]);
        } else {
          logger.warn("FCM send failed", {code: response.error.code});
        }
      }
    });
  }

  await pruneInvalidTokens(invalidTokens);

  logger.info("FCM send result", {
    successCount,
    failureCount,
    targetCount: tokens.length,
    prunedInvalidCount: invalidTokens.length,
  });
};

export const notifyDailyQuestionCreated = onDocumentCreated(
  "question_data/{questionId}",
  async (event) => {
    const questionId = event.params.questionId;
    const question = event.data?.data() as FirestoreData | undefined;

    if (!question) {
      logger.warn("Question document missing", {questionId});
      return;
    }

    const groupId = String(question._questionGroupDocumentId ?? "");
    if (!groupId) {
      logger.warn("Question group id is empty", {questionId});
      return;
    }

    const memberIds = await getGroupMemberIds(groupId);
    if (memberIds === null) {
      logger.warn("Question group not found", {questionId, groupId});
      return;
    }
    const tokens = await getUserTokens(memberIds);

    await sendPushToTokens(
      tokens,
      "오늘의 질문",
      "오늘의 질문이 도착했어요. 지금 답해보세요!",
      {
        type: "daily_question",
        groupId,
        questionId,
        questionListId: String(question._questionListDocumentId ?? ""),
      },
    );
  },
);

export const notifyRequestCreated = onDocumentCreated(
  "request_data/{requestId}",
  async (event) => {
    const requestId = event.params.requestId;
    const request = event.data?.data() as FirestoreData | undefined;

    if (!request) {
      logger.warn("Request document missing", {requestId});
      return;
    }

    const groupId = String(request._requestGroupDocumentID ?? "");
    const requesterUserId = String(request._requestUserDocumentID ?? "");

    if (!groupId) {
      logger.warn("Request group id is empty", {requestId});
      return;
    }

    const memberIds = await getGroupMemberIds(groupId);
    if (memberIds === null) {
      logger.warn("Request group not found", {requestId, groupId});
      return;
    }

    // 그룹 멤버가 아닌 사용자가 만든 요청으로 그룹 전체에 푸시가 가지 않게 한다.
    if (!requesterUserId || !memberIds.includes(requesterUserId)) {
      logger.warn("Requester is not a group member, skip notification", {
        requestId,
        groupId,
        requesterUserId,
      });
      return;
    }

    const tokens = await getUserTokens(
      memberIds,
      new Set([requesterUserId]),
    );
    const requesterName = await getUserNameById(requesterUserId);
    const body = requesterName ?
      `${requesterName}님이 요청을 남겼어요.` :
      "새로운 요청이 도착했어요.";

    await sendPushToTokens(
      tokens,
      "요청 알림",
      body,
      {
        type: "request",
        groupId,
        requestId,
        requesterUserId,
      },
    );
  },
);

export const notifyResponseCreated = onDocumentCreated(
  "request_data/{requestId}/response_data/{responseId}",
  async (event) => {
    const requestId = event.params.requestId;
    const responseId = event.params.responseId;
    const response = event.data?.data() as FirestoreData | undefined;

    if (!response) {
      logger.warn("Response document missing", {requestId, responseId});
      return;
    }

    const responderUserId = String(response._responseUserId ?? "");
    const requestSnap = await db.collection("request_data")
      .doc(requestId)
      .get();
    const request = requestSnap.data() as FirestoreData | undefined;

    if (!request) {
      logger.warn("Parent request not found", {requestId, responseId});
      return;
    }

    const requesterUserId = String(request._requestUserDocumentID ?? "");
    const groupId = String(request._requestGroupDocumentID ?? "");

    if (!requesterUserId) {
      logger.warn("Requester user id is empty", {requestId, responseId});
      return;
    }

    if (requesterUserId === responderUserId) {
      logger.info("Skip self response notification", {requestId, responseId});
      return;
    }

    if (!groupId) {
      logger.warn("Request group id is empty", {requestId, responseId});
      return;
    }

    const memberIds = await getGroupMemberIds(groupId);
    if (memberIds === null) {
      logger.warn("Request group not found", {requestId, responseId, groupId});
      return;
    }

    // 요청이 속한 그룹의 멤버가 아닌 사용자의 응답으로는 요청자에게 푸시하지 않는다.
    if (!responderUserId || !memberIds.includes(responderUserId)) {
      logger.warn("Responder is not a group member, skip notification", {
        requestId,
        responseId,
        groupId,
        responderUserId,
      });
      return;
    }

    const tokens = await getUserTokens([requesterUserId]);
    const responderName = await getUserNameById(responderUserId);
    const body = responderName ?
      `${responderName}님이 요청에 대한 응답을 남겼어요.` :
      "요청에 대한 응답이 도착했어요.";

    await sendPushToTokens(
      tokens,
      "응답 알림",
      body,
      {
        type: "response",
        groupId,
        requestId,
        responseId,
        requesterUserId,
        responderUserId,
      },
    );
  },
);
