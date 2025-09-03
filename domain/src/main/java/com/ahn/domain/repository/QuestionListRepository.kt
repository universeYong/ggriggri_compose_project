package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.QuestionList
import kotlinx.coroutines.flow.Flow

interface QuestionListRepository {
    suspend fun create(question: QuestionList): Flow<DataResourceResult<Unit>>
    suspend fun read(): Flow<DataResourceResult<List<QuestionList>>>
    suspend fun update(question: QuestionList): Flow<DataResourceResult<Unit>>
    suspend fun delete(questionId: String): Flow<DataResourceResult<Unit>>
}