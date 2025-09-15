package com.ahn.data.repository

import com.ahn.data.datasource.RequestDataSource
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Request
import com.ahn.domain.repository.RequestRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class FirestoreRequestRepositoryImpl @Inject constructor(val requestDataSource: RequestDataSource) : RequestRepository {
    override suspend fun create(request: Request): Flow<DataResourceResult<Unit>> = flow{
        emit(DataResourceResult.Loading)
        emit(requestDataSource.create(request))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun read(groupId: String): Flow<DataResourceResult<List<Request>>> = flow{
        emit(DataResourceResult.Loading)
        requestDataSource.read(groupId).collect { result ->
            emit(result)
        }
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun readAllRequests(groupId: String): Flow<DataResourceResult<List<Request>>> = flow{
        emit(DataResourceResult.Loading)
        requestDataSource.readAllRequests(groupId).collect { result ->
            emit(result)
        }
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun update(request: Request): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(requestId: String): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }
}