package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    suspend fun create(question: Question): Flow<DataResourceResult<Unit>>
    suspend fun read(): Flow<DataResourceResult<List<Question>>>
    suspend fun update(question: Question): Flow<DataResourceResult<Unit>>
    suspend fun delete(questionId: String): Flow<DataResourceResult<Unit>>
    suspend fun getQuestionForGroupAndDate(groupId: String, dateTimestamp: Long): Flow<DataResourceResult<Question?>>
}