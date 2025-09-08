package com.ahn.data.remote.firebase

import com.ahn.data.datasource.AnswerDataSource
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Answer

class FirestoreAnswerDataSourceImpl: AnswerDataSource {
    override suspend fun delete(answerId: String): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun create(answer: Answer): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun update(answer: Answer): DataResourceResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun read(): DataResourceResult<List<Answer>> {
        TODO("Not yet implemented")
    }
}