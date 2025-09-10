package com.ahn.data.repository

import com.ahn.data.datasource.AnswerDataSource
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Answer
import com.ahn.domain.repository.AnswerRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class FirestoreAnswerRepositoryImpl @Inject constructor(val answerDataSource: AnswerDataSource)
    : AnswerRepository {
    override suspend fun create(
        questionId: String,
        answerInfo: Answer,
    ): Flow<DataResourceResult<String?>> = flow {
        emit(DataResourceResult.Loading)
        val result = answerDataSource.create(questionId, answerInfo)
        emit(result)
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun read(questionId: String): Flow<DataResourceResult<List<Answer>>> = flow {
        emit(DataResourceResult.Loading)
        emit(answerDataSource.read(questionId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun update(
        questionId: String,
        answerInfo: Answer,
    ): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(answerDataSource.update(questionId, answerInfo))
    }.catch { emit(DataResourceResult.Failure(it)) }


    override suspend fun delete(
        questionId: String,
        answerId: String,
    ): Flow<DataResourceResult<Unit>> = flow{
        emit(DataResourceResult.Loading)
        emit(answerDataSource.delete(questionId, answerId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun getAllAnswersForQuestion(questionDataDocumentId: String): Flow<DataResourceResult<List<Answer>>> = flow{
        emit(DataResourceResult.Loading)
        emit(answerDataSource.getAllAnswersForQuestion(questionDataDocumentId))
    }.catch { emit(DataResourceResult.Failure(it))  }

}