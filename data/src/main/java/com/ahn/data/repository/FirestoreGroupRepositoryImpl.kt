package com.ahn.data.repository

import com.ahn.data.datasource.GroupDataSource
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group
import com.ahn.domain.repository.GroupRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class FirestoreGroupRepositoryImpl @Inject constructor(val groupDataSource: GroupDataSource):
    GroupRepository  {
    override suspend fun create(groupInfo: Group): Flow<DataResourceResult<String>> = flow {
        emit(DataResourceResult.Loading)
        emit(groupDataSource.create(groupInfo))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun read(): Flow<DataResourceResult<List<Group>>> {
        TODO("Not yet implemented")
    }

    override suspend fun update(groupInfo: Group): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(groupDataSource.update(groupInfo))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun delete(groupId: String): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(groupDataSource.delete(groupId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun getGroupByCode(groupCode: String): Flow<DataResourceResult<Group?>> = flow{
        emit(DataResourceResult.Loading)
        emit(groupDataSource.getGroupByCode(groupCode))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun getGroupById(groupId: String): Flow<DataResourceResult<Group?>> = flow {
        emit(DataResourceResult.Loading)
        emit(groupDataSource.getGroupById(groupId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun addUserToGroup(
        groupId: String,
        userId: String,
    ): Flow<DataResourceResult<Unit>> = flow{
        emit(DataResourceResult.Loading)
        emit(groupDataSource.addUserToGroup(groupId, userId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun removeUserFromGroup(
        groupId: String,
        userId: String,
    ): Flow<DataResourceResult<Unit>> = flow {
        emit(DataResourceResult.Loading)
        emit(groupDataSource.removeUserFromGroup(groupId, userId))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun isGroupCodeExist(groupCode: String): Flow<DataResourceResult<Boolean>> = flow{
        emit(DataResourceResult.Loading)
        emit(groupDataSource.isGroupCodeExist(groupCode))
    }.catch { emit(DataResourceResult.Failure(it)) }

    override suspend fun getGroupMembers(groupId: String): Flow<DataResourceResult<List<String>>> = flow{
        emit(DataResourceResult.Loading)
        emit(groupDataSource.getGroupMembers(groupId))
    }.catch { emit(DataResourceResult.Failure(it)) }
}