package com.ahn.data.mapper

import com.ahn.data.remote.dto.QuestionDTO
import com.ahn.domain.model.Question


fun QuestionDTO.toDomainQuestion(documentId: String): Question = Question(
    questionId = documentId,
    questionCreatedTime = _questionCreatedTime,
    questionGroupDocumentId = _questionGroupDocumentId,
    questionListDocumentId = _questionListDocumentId
)

fun Question.toFirestoreQuestionDTO() = mapOf(
    "_questionCreatedTime" to this.questionCreatedTime,
    "_questionGroupDocumentId" to this.questionGroupDocumentId,
    "_questionListDocumentId" to this.questionListDocumentId
)
