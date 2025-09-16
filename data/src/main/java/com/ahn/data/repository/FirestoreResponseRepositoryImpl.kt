package com.ahn.data.repository

import com.ahn.data.datasource.ResponseDataSource
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Response
import com.ahn.domain.repository.ResponseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FirestoreResponseRepositoryImpl @Inject constructor(
    private val responseDataSource: ResponseDataSource
) : ResponseRepository {

    override suspend fun create(requestDocumentId: String, response: Response): Flow<DataResourceResult<Response>> = flow {
        emit(DataResourceResult.Loading)
        emit(responseDataSource.create(requestDocumentId, response))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun read(requestDocumentId: String): Flow<DataResourceResult<List<Response>>> = flow {
        emit(DataResourceResult.Loading)
        emit(responseDataSource.read(requestDocumentId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun update(requestDocumentId: String, response: Response): Flow<DataResourceResult<Response>> = flow {
        emit(DataResourceResult.Loading)
        emit(responseDataSource.update(requestDocumentId, response))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun delete(requestDocumentId: String, responseId: String): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(responseDataSource.delete(requestDocumentId, responseId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun getResponseById(requestDocumentId: String, responseId: String): Flow<DataResourceResult<Response?>> = flow {
        emit(DataResourceResult.Loading)
        emit(responseDataSource.getResponseById(requestDocumentId, responseId))
    }.catch { emit(DataResourceResult.Failure(it)) }

}
