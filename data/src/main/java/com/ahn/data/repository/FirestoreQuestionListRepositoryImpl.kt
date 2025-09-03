package com.ahn.data.repository

import com.ahn.data.datasource.QuestionListDataSource
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.QuestionList
import com.ahn.domain.repository.QuestionListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class FirestoreQuestionListRepositoryImpl(val questionListDataSource: QuestionListDataSource):
    QuestionListRepository {
    override suspend fun create(question: QuestionList): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun read(): Flow<DataResourceResult<List<QuestionList>>> = flow{
        emit(DataResourceResult.Loading)
        emit(questionListDataSource.read())
    }.catch { emit(DataResourceResult.Failure(it))}

    override suspend fun update(question: QuestionList): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(questionId: String): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }
}