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

const pruneInvalidTokens = async (
  invalidTokens: string[],
): Promise<void> => {
  const uniqueInvalidTokens = [...new Set(invalidTokens)];
  if (uniqueInvalidTokens.length === 0) {
    return;
  }

  const userSnapshot = await userCollection.get();
  const updates: Array<Promise<unknown>> = [];

  for (const doc of userSnapshot.docs) {
    const userTokens = normalizeTokens(doc.data()?._userFcmToken);
    const tokensToRemove = userTokens.filter((token) => {
      return uniqueInvalidTokens.includes(token);
    });

    if (tokensToRemove.length === 0) {
      continue;
    }

    updates.push(doc.ref.update({
      _userFcmToken: admin.firestore.FieldValue.arrayRemove(...tokensToRemove),
    }));
  }

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

  const message: admin.messaging.MulticastMessage = {
    tokens,
    notification: {title, body},
    data: toStringData(data),
  };

  const result = await messaging.sendEachForMulticast(message);
  const invalidTokens: string[] = [];

  result.responses.forEach((response, index) => {
    if (!response.success && response.error) {
      if (PRUNE_ERROR_CODES.has(response.error.code)) {
        invalidTokens.push(tokens[index]);
      }
    }
  });

  await pruneInvalidTokens(invalidTokens);

  logger.info("FCM send result", {
    successCount: result.successCount,
    failureCount: result.failureCount,
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

    const groupSnap = await db.collection("group_data").doc(groupId).get();
    const memberIds =
      (groupSnap.data()?._groupUserDocumentID ?? []) as string[];
    const tokens = await getUserTokens(memberIds);

    await sendPushToTokens(
      tokens,
      "New daily question",
      "Check today's question now.",
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

    const groupSnap = await db.collection("group_data").doc(groupId).get();
    const memberIds =
      (groupSnap.data()?._groupUserDocumentID ?? []) as string[];
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
