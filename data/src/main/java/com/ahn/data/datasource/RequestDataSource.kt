package com.ahn.data.datasource

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Request
import kotlinx.coroutines.flow.Flow

interface RequestDataSource {
    suspend fun delete(requestId: String): DataResourceResult<Unit>
    suspend fun create(request: Request): DataResourceResult<Unit>
    suspend fun update(request: Request): DataResourceResult<Unit>
    suspend fun read(groupId: String): Flow<DataResourceResult<List<Request>>>
    suspend fun readAllRequests(groupId: String): Flow<DataResourceResult<List<Request>>>
}