package com.ahn.data.mapper

import com.ahn.data.remote.dto.AnswerDTO
import com.ahn.data.remote.dto.GroupDTO
import com.ahn.domain.model.Answer
import com.ahn.domain.model.Group


fun AnswerDTO.toDomainAnswer(documentId: String): Answer = Answer(
    answerId = documentId,
    answerMessage = _answerMessage,
    answerResponseTime = _answerResponseTime,
    answerRequestState = _answerRequestState,
    answerUserDocumentID = _answerUserDocumentID
)

fun Answer.toFirestoreAnswerDTO() = mapOf(
    "_answerMessage" to this.answerMessage,
    "_answerResponseTime" to this.answerResponseTime,
    "_answerRequestState" to this.answerRequestState,
    "_answerUserDocumentID" to this.answerUserDocumentID
)