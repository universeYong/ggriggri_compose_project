package com.ahn.data.datasource

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Answer

interface AnswerDataSource {
    suspend fun delete(answerId: String): DataResourceResult<Unit>
    suspend fun create(answer: Answer): DataResourceResult<Unit>
    suspend fun update(answer: Answer): DataResourceResult<Unit>
    suspend fun read(): DataResourceResult<List<Answer>>
}