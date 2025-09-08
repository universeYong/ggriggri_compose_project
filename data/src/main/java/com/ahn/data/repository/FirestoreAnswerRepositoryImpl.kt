package com.ahn.data.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Answer
import com.ahn.domain.repository.AnswerRepository
import kotlinx.coroutines.flow.Flow

class FirestoreAnswerRepositoryImpl: AnswerRepository {
    override suspend fun create(answerInfo: Answer): Flow<DataResourceResult<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun read(): Flow<DataResourceResult<List<Answer>>> {
        TODO("Not yet implemented")
    }

    override suspend fun update(answerInfo: Answer): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(answerId: String): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }
}