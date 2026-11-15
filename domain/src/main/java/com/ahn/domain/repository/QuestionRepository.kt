package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    suspend fun create(question: Question): Flow<DataResourceResult<Question>>
    suspend fun getQuestionForGroupAndDate(groupId: String, dateTimestamp: Long): Flow<DataResourceResult<Question?>>
    fun getQuestionRecordById(documentId: String): Flow<DataResourceResult<Question?>>
    fun getAllQuestionsForGroup(groupId: String): Flow<DataResourceResult<List<Question>>>
}