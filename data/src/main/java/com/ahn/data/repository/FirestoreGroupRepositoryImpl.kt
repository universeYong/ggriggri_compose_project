package com.ahn.data.repository

import com.ahn.data.datasource.GroupDataSource
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group
import com.ahn.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class FirestoreGroupRepositoryImpl(val targetDataSource: GroupDataSource): GroupRepository  {
    override suspend fun create(groupInfo: Group): Flow<DataResourceResult<String>> = flow {
        emit(DataResourceResult.Loading)
        emit(targetDataSource.create(groupInfo))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun read(): Flow<DataResourceResult<List<Group>>> {
        TODO("Not yet implemented")
    }

    override suspend fun update(groupInfo: Group): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(groupId: String): Flow<DataResourceResult<Unit>> {
        TODO("Not yet implemented")
    }
}