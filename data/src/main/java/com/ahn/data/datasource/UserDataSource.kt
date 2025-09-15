package com.ahn.data.datasource

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.User

interface UserDataSource {
    suspend fun delete(userId: String): DataResourceResult<Unit>
    suspend fun create(user: User): DataResourceResult<Unit>
    suspend fun update(user: User): DataResourceResult<Unit>
    suspend fun read(): DataResourceResult<List<User>>
    suspend fun getUserById(userId: String): DataResourceResult<User?>
    suspend fun getUserByIdSync(userId: String): DataResourceResult<User?>
    suspend fun getUserGroupDocumentId(userId: String): DataResourceResult<String?>
    suspend fun updateUserGroupDocumentId(userId: String, groupDocumentId: String): DataResourceResult<Unit>
}