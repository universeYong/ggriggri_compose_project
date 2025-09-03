package com.ahn.data.datasource

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.QuestionList

interface QuestionListDataSource {
    suspend fun delete(questionId: String): DataResourceResult<Unit>
    suspend fun create(question: QuestionList): DataResourceResult<Unit>
    suspend fun update(question: QuestionList): DataResourceResult<Unit>
    suspend fun read(): DataResourceResult<List<QuestionList>>
}