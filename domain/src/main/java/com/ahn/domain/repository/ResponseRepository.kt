package com.ahn.domain.repository

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Response
import kotlinx.coroutines.flow.Flow

interface ResponseRepository {
    suspend fun create(requestDocumentId: String, response: Response): Flow<DataResourceResult<Response>>
    suspend fun read(requestDocumentId: String): Flow<DataResourceResult<List<Response>>>
    suspend fun update(requestDocumentId: String, response: Response): Flow<DataResourceResult<Response>>
    suspend fun delete(requestDocumentId: String, responseId: String): Flow<DataResourceResult<Unit>>
    suspend fun getResponseById(requestDocumentId: String, responseId: String): Flow<DataResourceResult<Response?>>
}
