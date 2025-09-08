package com.ahn.data.datasource

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Question

interface QuestionDataSource {
    suspend fun delete(questionId: String): DataResourceResult<Unit>
    suspend fun create(question: Question): DataResourceResult<Question>
    suspend fun update(question: Question): DataResourceResult<Unit>
    suspend fun read(): DataResourceResult<List<Question>>
    suspend fun getQuestionForGroupAndDate(groupId: String, dateTimestamp: Long): DataResourceResult<Question?>
    suspend fun getQuestionRecordById(documentId: String): DataResourceResult<Question?>
    suspend fun getAllQuestionsForGroup(groupId: String): DataResourceResult<List<Question>>
}