package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.QuestionList
import kotlinx.coroutines.flow.Flow

interface QuestionListRepository {
    suspend fun read(): Flow<DataResourceResult<List<QuestionList>>>
}