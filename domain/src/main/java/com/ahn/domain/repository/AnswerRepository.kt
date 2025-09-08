package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Answer
import kotlinx.coroutines.flow.Flow

interface AnswerRepository {
    suspend fun create(questionId: String, answerInfo: Answer): Flow<DataResourceResult<String?>>
    suspend fun read(questionId: String): Flow<DataResourceResult<List<Answer>>>
    suspend fun update(questionId: String, answerInfo: Answer): Flow<DataResourceResult<Unit>>
    suspend fun delete(questionId: String, answerId: String): Flow<DataResourceResult<Unit>>
    suspend fun getAllAnswersForQuestion(questionDataDocumentId: String): Flow<DataResourceResult<List<Answer>>>
}