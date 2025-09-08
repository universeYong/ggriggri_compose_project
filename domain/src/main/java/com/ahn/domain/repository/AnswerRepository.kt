package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Answer
import kotlinx.coroutines.flow.Flow

interface AnswerRepository {
    suspend fun create(answerInfo: Answer): Flow<DataResourceResult<String>>
    suspend fun read(): Flow<DataResourceResult<List<Answer>>>
    suspend fun update(answerInfo: Answer): Flow<DataResourceResult<Unit>>
    suspend fun delete(answerId: String): Flow<DataResourceResult<Unit>>
}