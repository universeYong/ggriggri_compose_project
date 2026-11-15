package com.ahn.data.datasource

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.QuestionList

interface QuestionListDataSource {
    suspend fun read(): DataResourceResult<List<QuestionList>>
}