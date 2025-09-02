package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    suspend fun create(groupInfo: Group): Flow<DataResourceResult<String>>
    suspend fun read(): Flow<DataResourceResult<List<Group>>>
    suspend fun update(groupInfo: Group): Flow<DataResourceResult<Unit>>
    suspend fun delete(groupId: String): Flow<DataResourceResult<Unit>>
    suspend fun getGroupByCode(groupCode: String): Flow<DataResourceResult<Group?>>
    suspend fun addUserToGroup(groupId: String, userId: String): Flow<DataResourceResult<Unit>>
    suspend fun isGroupCodeExist(groupCode: String): Flow<DataResourceResult<Boolean>>
    suspend fun getGroupMembers(groupId: String): Flow<DataResourceResult<List<String>>>
}