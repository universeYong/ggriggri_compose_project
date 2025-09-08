package com.ahn.data.datasource

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Answer

interface AnswerDataSource {
    suspend fun delete(questionId: String ,answerId: String): DataResourceResult<Unit>
    suspend fun create(questionId: String, answer: Answer): DataResourceResult<String?>
    suspend fun update(questionId: String, answer: Answer): DataResourceResult<Unit>
    suspend fun read(questionId: String): DataResourceResult<List<Answer>>
    suspend fun getAllAnswersForQuestion(questionDataDocumentId: String): DataResourceResult<List<Answer>>
}