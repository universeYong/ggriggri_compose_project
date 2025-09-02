package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.User
import kotlinx.coroutines.flow.Flow


interface UserRepository {
    suspend fun create(userInfo: User): Flow<DataResourceResult<Unit>>
    suspend fun read(): Flow<DataResourceResult<List<User>>>
    suspend fun update(userInfo: User): Flow<DataResourceResult<Unit>>
    suspend fun delete(userId: String): Flow<DataResourceResult<Unit>>
    suspend fun getUserGroupDocumentId(userId: String): Flow<DataResourceResult<String?>>
    suspend fun updateUserGroupDocumentId(userId: String, groupDocumentId: String):
            Flow<DataResourceResult<Unit>>
    suspend fun getUserById(userId: String): Flow<DataResourceResult<User?>>

}