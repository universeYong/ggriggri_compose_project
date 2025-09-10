package com.ahn.data.repository

import com.ahn.data.datasource.QuestionDataSource
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Question
import com.ahn.domain.repository.QuestionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class FirestoreQuestionRepositoryImpl @Inject constructor(val questionDataSource: QuestionDataSource) :
    QuestionRepository {
    override suspend fun create(question: Question): Flow<DataResourceResult<Question>> = flow{
        emit(DataResourceResult.Loading)
        emit(questionDataSource.create(question))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun read(): Flow<DataResourceResult<List<Question>>> {
        TODO("Not yet implemented")
    }

    override suspend fun update(question: Question): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(questionId: String): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun getQuestionForGroupAndDate(
        groupId: String,
        dateTimestamp: Long,
    ): Flow<DataResourceResult<Question?>> = flow{
        emit(DataResourceResult.Loading)
        emit(questionDataSource.getQuestionForGroupAndDate(groupId, dateTimestamp))
    }.catch { exception ->
        emit(DataResourceResult.Failure(exception))
    }

    override fun getQuestionRecordById(documentId: String): Flow<DataResourceResult<Question?>> = flow{
        emit(DataResourceResult.Loading)
        emit(questionDataSource.getQuestionRecordById(documentId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override fun getAllQuestionsForGroup(groupId: String): Flow<DataResourceResult<List<Question>>> = flow{
        emit(DataResourceResult.Loading)
        emit(questionDataSource.getAllQuestionsForGroup(groupId))
    }.catch { emit(DataResourceResult.Failure(it)) }
}