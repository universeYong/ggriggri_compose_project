package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Request
import kotlinx.coroutines.flow.Flow

interface RequestRepository {
    suspend fun create(request: Request): Flow<DataResourceResult<Unit>>
    suspend fun read(groupId: String): Flow<DataResourceResult<List<Request>>>
    suspend fun readAllRequests(groupId: String): Flow<DataResourceResult<List<Request>>>

    /** 요청 문서 하나를 실시간으로 구독한다. 문서가 없으면 Success(null). */
    suspend fun getRequestById(requestId: String): Flow<DataResourceResult<Request?>>
    suspend fun update(request: Request): Flow<DataResourceResult<Unit>>
    suspend fun delete(requestId: String): Flow<DataResourceResult<Unit>>
}