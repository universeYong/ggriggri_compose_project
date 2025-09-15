package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Request
import kotlinx.coroutines.flow.Flow

interface RequestRepository {
    suspend fun create(request: Request): Flow<DataResourceResult<Unit>>
    suspend fun read(groupId: String): Flow<DataResourceResult<List<Request>>>
    suspend fun readAllRequests(groupId: String): Flow<DataResourceResult<List<Request>>>
    suspend fun update(request: Request): Flow<DataResourceResult<Unit>>
    suspend fun delete(requestId: String): Flow<DataResourceResult<Unit>>
}