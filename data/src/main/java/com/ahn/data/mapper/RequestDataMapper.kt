package com.ahn.data.mapper

import com.ahn.data.remote.dto.RequestDTO
import com.ahn.domain.model.Request


fun RequestDTO.toDomainRequest(documentId: String): Request = Request(
    requestId = documentId, // Firebase 문서 ID를 requestId로 사용
    requestTime = _requestTime,
    requestUserDocumentID = _requestUserDocumentID,
    requestMessage = _requestMessage,
    requestImage = _requestImage,
    requestGroupDocumentID = _requestGroupDocumentID,
    answerDeadline = _answerDeadline,
    hasAnswer = _hasAnswer
)

fun Request.toFirestoreRequestDTO() = mapOf(
    "_requestId" to this.requestId,
    "_requestTime" to this.requestTime,
    "_requestUserDocumentID" to this.requestUserDocumentID,
    "_requestMessage" to this.requestMessage,
    "_requestImage" to this.requestImage,
    "_requestGroupDocumentID" to this.requestGroupDocumentID,
    "_answerDeadline" to this.answerDeadline,
    "_hasAnswer" to this.hasAnswer
)